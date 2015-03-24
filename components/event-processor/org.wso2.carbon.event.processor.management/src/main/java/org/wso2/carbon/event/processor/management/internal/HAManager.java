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
import org.wso2.carbon.event.processor.core.CEPMembership;
import org.wso2.carbon.event.processor.management.config.HAConfiguration;
import org.wso2.carbon.event.processor.management.internal.ds.EventProcessingManagementValueHolder;
import org.wso2.carbon.event.processor.management.internal.thrift.ManagementServiceClientThriftImpl;
import org.wso2.carbon.event.processor.management.internal.util.Constants;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

public class HAManager {
    private static final Log log = LogFactory.getLog(HAManager.class);

    private final HazelcastInstance hazelcastInstance;
    private boolean activeLockAcquired;
    private boolean passiveLockAcquired;
    private ILock activeLock;
    private ILock passiveLock;
    private IMap<CEPMembership, Boolean> members;
    private IMap<String, CEPMembership> roleToMembershipMap;

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    private ReadWriteLock readWriteLock;
    private Future stateChanger = null;
    private static String activeId;
    private static String passiveId;


    public HAManager(HazelcastInstance hazelcastInstance, HAConfiguration haConfiguration,
                     ReadWriteLock readWriteLock) {
        this.hazelcastInstance = hazelcastInstance;
        this.readWriteLock = readWriteLock;
        activeId = Constants.ACTIVEID;
        passiveId = Constants.PASSIVEID;
        activeLock = hazelcastInstance.getLock(activeId);
        passiveLock = hazelcastInstance.getLock(passiveId);

        members = hazelcastInstance.getMap(Constants.MEMBERS);
        members.put(EventProcessingManagementValueHolder.getCurrentCEPMembershipInfo(), true);

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

        roleToMembershipMap = hazelcastInstance.getMap(Constants.ROLE_MEMBERSHIP_MAP);
        roleToMembershipMap.addEntryListener(new EntryAdapter<String, CEPMembership>() {

            @Override
            public void entryRemoved(EntryEvent<String, CEPMembership> stringCEPMembershipEntryEvent) {
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
                } else {
                    becomePassive();
                }
            }
        } else if (!activeLockAcquired) {
            if (activeLock.tryLock()) {
                activeLockAcquired = true;
                becomeActive();
            }
        }
    }
    private void becomePassive() {
        roleToMembershipMap.put(passiveId, EventProcessingManagementValueHolder.getCurrentCEPMembershipInfo());

        try {
            readWriteLock.writeLock().tryLock(20000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Error in getting lock.", e);
        }

        CEPMembership activeMember = roleToMembershipMap.get(activeId);
        EventProcessingManager eventProcessingManager = EventProcessingManagementValueHolder.getEventProcessingManager();

        ManagementServiceClient client = new ManagementServiceClientThriftImpl();
        byte[] state = client.getSnapshot(activeMember);
        readWriteLock.writeLock().unlock();
    }

    private void becomeActive() {
        EventProcessingManager eventProcessingManager = EventProcessingManagementValueHolder.getEventProcessingManager();

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
