/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.processor.management.internal;

import com.hazelcast.core.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.processor.common.util.ByteSerializer;
import org.wso2.carbon.event.processor.management.EventProcessingManager;
import org.wso2.carbon.event.processor.management.config.HAConfiguration;
import org.wso2.carbon.event.processor.management.internal.ds.EventProcessingManagerValueHolder;
import org.wso2.carbon.event.processor.management.internal.thrift.ManagementServiceClientThriftImpl;
import org.wso2.carbon.event.processor.management.internal.util.Constants;

import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;


public class HAManager {
    private static final Log log = LogFactory.getLog(HAManager.class);

    private final HazelcastInstance hazelcastInstance;
    private HAConfiguration haConfiguration;
    private boolean activeLockAcquired;
    private boolean passiveLockAcquired;
    private ILock activeLock;
    private ILock passiveLock;
    private IMap<HAConfiguration, Boolean> members;
    private IMap<String, HAConfiguration> roleToMembershipMap;

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    private Lock writeLock;
    private Future stateChanger = null;
    private String activeId;
    private String passiveId;
    private EventProcessingManager eventProcessingManager;

    public HAManager(HazelcastInstance hazelcastInstance, HAConfiguration haConfiguration,
                     Lock writeLock, EventProcessingManager eventProcessingManager) {
        this.hazelcastInstance = hazelcastInstance;
        this.writeLock = writeLock;
        this.haConfiguration = haConfiguration;
        activeId = Constants.ACTIVEID;
        passiveId = Constants.PASSIVEID;
        activeLock = hazelcastInstance.getLock(activeId);
        passiveLock = hazelcastInstance.getLock(passiveId);

        members = hazelcastInstance.getMap(Constants.MEMBERS);
        members.set(haConfiguration, true);

        this.eventProcessingManager = eventProcessingManager;
        ManagementServer.start(haConfiguration);

        hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {
            @Override
            public void memberAdded(MembershipEvent membershipEvent) {

            }

            @Override
            public void memberRemoved(MembershipEvent membershipEvent) {
                if (!activeLockAcquired) {
                    tryChangeState();
                }
            }

            @Override
            public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {

            }
        });

        try {
            roleToMembershipMap = hazelcastInstance.getMap(Constants.ROLE_MEMBERSHIP_MAP);
        } catch (Exception e) {
            log.error(e);
        }
        roleToMembershipMap.addEntryListener(new EntryAdapter<String, HAConfiguration>() {

            @Override
            public void entryRemoved(EntryEvent<String, HAConfiguration> stringCEPMembershipEntryEvent) {
                tryChangeState();
            }

        }, activeId, false);

    }

    public void init() {
        tryChangeState();
        if (!activeLockAcquired) {
            scheduledThreadPoolExecutor.execute(new PeriodicStateChanger());
        }
    }

    private void tryChangeState() {
        if (!passiveLockAcquired) {
            if (passiveLock.tryLock()) {
                passiveLockAcquired = true;
                if (activeLock.tryLock()) {
                    activeLockAcquired = true;
                    becomeActive();
                    passiveLock.forceUnlock();
                } else {
                    becomePassive();
                }
            }
        } else if (!activeLockAcquired) {
            if (activeLock.tryLock()) {
                activeLockAcquired = true;
                becomeActive();
                passiveLock.forceUnlock();
            }
        }
    }

    private void becomePassive() {
        roleToMembershipMap.set(passiveId, haConfiguration);
        HAConfiguration activeMember = null;
        try {
            activeMember = roleToMembershipMap.get(activeId);
        } catch (Exception e) {
            log.error(e);
        }
        HAConfiguration passiveMember = roleToMembershipMap.get(passiveId);
        // Send non-duplicate events to active member
        eventProcessingManager.getEventReceiverManagementService().startServer(passiveMember.getTransport());
        eventProcessingManager.getEventReceiverManagementService().setOtherMember(activeMember.getTransport());
        eventProcessingManager.getEventReceiverManagementService().start();

        eventProcessingManager.getEventReceiverManagementService().pause();
        eventProcessingManager.getEventProcessorManagementService().pause();
        eventProcessingManager.getEventPublisherManagementService().setDrop(true);
        ManagementServiceClient client = new ManagementServiceClientThriftImpl();

        byte[] state = null;
        try {
            state =client.getSnapshot(activeMember.getManagement());
        } catch(Throwable e) {
            log.error(e);
        }
        ArrayList<byte[]> stateList = (ArrayList<byte[]>) ByteSerializer.BToO(state);

        // Synchronize the duplicate events with active member
        eventProcessingManager.getEventReceiverManagementService().syncState(stateList.get(1));

        eventProcessingManager.getEventProcessorManagementService().restoreState(stateList.get(0));
        eventProcessingManager.getEventProcessorManagementService().resume();
        eventProcessingManager.getEventReceiverManagementService().resume();

        writeLock.unlock();
    }

    private void becomeActive() {
        EventProcessingManager eventProcessingManager = EventProcessingManagerValueHolder.getEventProcessingManager();

        roleToMembershipMap.set(activeId, haConfiguration);
        eventProcessingManager.getEventReceiverManagementService().startServer(haConfiguration.getTransport());

        eventProcessingManager.getEventPublisherManagementService().setDrop(false);
        eventProcessingManager.getEventReceiverManagementService().start();
    }

    public byte[] getState() {
        eventProcessingManager.getEventReceiverManagementService().pause();
        eventProcessingManager.getEventProcessorManagementService().pause();

        HAConfiguration passiveMember = roleToMembershipMap.get(passiveId);
        eventProcessingManager.getEventReceiverManagementService().setOtherMember(passiveMember.getTransport());

        byte[] processorState = eventProcessingManager.getEventProcessorManagementService().getState();
        byte[] receiverState = eventProcessingManager.getEventReceiverManagementService().getState();

        ArrayList<byte[]> stateList = new ArrayList<byte[]>(2);
        stateList.add(processorState);
        stateList.add(receiverState);

        byte[] state = ByteSerializer.OToB(stateList);

        eventProcessingManager.getEventProcessorManagementService().resume();
        eventProcessingManager.getEventReceiverManagementService().resume();

        return state;
    }

    public void shutdown() {
        if (passiveLockAcquired) {
            roleToMembershipMap.remove(passiveId);
            passiveLock.forceUnlock();
        }
        if (activeLockAcquired) {
            activeLock.forceUnlock();
            roleToMembershipMap.remove(activeId);
        }
        stateChanger.cancel(false);
    }

    class PeriodicStateChanger implements Runnable {

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            tryChangeState();
            if (!activeLockAcquired) {
                stateChanger = scheduledThreadPoolExecutor.schedule(this, 15, TimeUnit.SECONDS);
            }
        }
    }
}
