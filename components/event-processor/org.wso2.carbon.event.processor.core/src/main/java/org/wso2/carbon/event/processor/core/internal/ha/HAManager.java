/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.processor.core.internal.ha;

import com.hazelcast.core.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.util.ByteSerializer;
import org.wso2.carbon.event.processor.management.CEPMembership;
import org.wso2.siddhi.core.ExecutionPlanRuntime;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;

public class HAManager {
    private static final Log log = LogFactory.getLog(HAManager.class);

    private final String executionPlanName;
    private final int tenantId;
    private final ExecutionPlanRuntime executionPlanRuntime;
    private final CEPMembership currentCepMembershipInfo;
    private Lock readLock;
    private boolean isActive;
    private boolean isPassive;
    private final Map<String, SiddhiHAInputEventDispatcher> inputEventDispatcherMap = new HashMap<String, SiddhiHAInputEventDispatcher>();
    private List<SiddhiHAOutputStreamListener> streamCallbackList = new ArrayList<SiddhiHAOutputStreamListener>();

    private ThreadPoolExecutor processThreadPoolExecutor;


    public HAManager(HazelcastInstance hazelcastInstance, String executionPlanName, int tenantId, ExecutionPlanRuntime executionPlanRuntime, int inputProcessors, CEPMembership currentCepMembershipInfo) {
        this.executionPlanName = executionPlanName;
        this.tenantId = tenantId;
        this.executionPlanRuntime = executionPlanRuntime;
        this.currentCepMembershipInfo = currentCepMembershipInfo;
        processThreadPoolExecutor = new ThreadPoolExecutor(inputProcessors, inputProcessors,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        readLock = EventProcessorValueHolder.getEventProcessorManagementService().getReadLock();
    }


    public ExecutorService getProcessThreadPoolExecutor() {
        return processThreadPoolExecutor;
    }

    public void addStreamCallback(SiddhiHAOutputStreamListener streamCallback) {
        streamCallbackList.add(streamCallback);
    }

    public void addInputEventDispatcher(String streamId, SiddhiHAInputEventDispatcher eventDispatcher) {
        inputEventDispatcherMap.put(streamId, eventDispatcher);
        eventDispatcher.setReadLock(readLock);
    }

    public void shutdown() {

    }

    public void init() {

    }

    public SnapshotData getActiveSnapshotData() {
        SnapshotData snapshotData = new SnapshotData();

        HashMap<String, Object[]> eventMap = new HashMap<String, Object[]>();
        for (Map.Entry<String, SiddhiHAInputEventDispatcher> entry : inputEventDispatcherMap.entrySet()) {
            Object[] activeEventData = entry.getValue().getEventQueue().peek();
            eventMap.put(entry.getKey(), activeEventData);
        }

        snapshotData.setNextEventData(ByteSerializer.OToB(eventMap));
        snapshotData.setStates(executionPlanRuntime.snapshot());
        return snapshotData;
    }


    public void restoreSnapshotData(SnapshotData snapshotData) {
        try {
            executionPlanRuntime.restore(snapshotData.getStates());
            byte[] eventData = snapshotData.getNextEventData();
            HashMap<String, Object[]> eventMap = (HashMap<String, Object[]>) ByteSerializer.BToO(eventData);
            for (Map.Entry<String, Object[]> entry : eventMap.entrySet()) {
                SiddhiHAInputEventDispatcher inputEventDispatcher = inputEventDispatcherMap.get(entry.getKey());
                if(inputEventDispatcher==null){
                    throw new Exception(entry.getKey() +" stream mismatched with the Active Node " + executionPlanName +" execution plan for tenant:" + tenantId );
                }
                BlockingQueue<Object[]> eventQueue = inputEventDispatcher.getEventQueue();
                Object[] activeEventData = entry.getValue();
                Object[] passiveEventData = eventQueue.peek();
                while (!Arrays.equals(passiveEventData, activeEventData)) {
                    eventQueue.remove();
                    passiveEventData = eventQueue.peek();
                }
            }
        } catch (Throwable t) {
            log.error("Syncing failed when becoming a Passive Node for tenant:" + tenantId + " on:" + executionPlanName +" execution plan", t);

        }
    }

    public void becomePassive(CEPMembership activeMember, Set<CEPMembership> members) {
        for (SiddhiHAOutputStreamListener streamCallback : streamCallbackList) {
            streamCallback.setDrop(true);
        }
    }

    public void becomeActive(Set<CEPMembership> members) {
        for (SiddhiHAOutputStreamListener streamCallback : streamCallbackList) {
            streamCallback.setDrop(false);
        }
        log.info("Became Active Member for tenant:" + tenantId + " on:" + executionPlanName);
    }

    public boolean isActiveMember() {
        return isActive;
    }

    public boolean isPassiveMember() {
        return isPassive;
    }
}
