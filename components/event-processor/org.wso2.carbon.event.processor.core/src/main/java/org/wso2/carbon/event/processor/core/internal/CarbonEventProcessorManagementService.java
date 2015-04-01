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

package org.wso2.carbon.event.processor.core.internal;

import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.core.EventProcessorManagementService;
import org.wso2.carbon.event.processor.core.ExecutionPlan;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.common.util.ByteSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CarbonEventProcessorManagementService implements EventProcessorManagementService {

    private static Logger log = Logger.getLogger(CarbonEventProcessorManagementService.class);
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public byte[] getState() {
        Map<Integer, TreeMap<String, ExecutionPlan>> map
                = EventProcessorValueHolder.getEventProcessorService().getTenantSpecificExecutionPlans();
        HashMap<Integer, HashMap<String, byte[]>> snapshotdata = new HashMap<Integer, HashMap<String, byte[]>>();

        for (Map.Entry<Integer, TreeMap<String, ExecutionPlan>> tenantEntry : map.entrySet()) {
            HashMap<String, byte[]> tenantData = new HashMap<String, byte[]>();
            for (Map.Entry<String, ExecutionPlan> executionPlanData : tenantEntry.getValue().entrySet()) {
                tenantData.put(executionPlanData.getKey(), executionPlanData.getValue().getExecutionPlanRuntime().snapshot());
            }
            snapshotdata.put(tenantEntry.getKey(), tenantData);
        }
        return ByteSerializer.OToB(snapshotdata);
    }

    public void restoreState(byte[] bytes) {
        Map<Integer, TreeMap<String, ExecutionPlan>> map
                = EventProcessorValueHolder.getEventProcessorService().getTenantSpecificExecutionPlans();
        HashMap<Integer, HashMap<String, byte[]>> snapshotdataList = (HashMap<Integer, HashMap<String, byte[]>>) ByteSerializer.BToO(bytes);

        for (Map.Entry<Integer, TreeMap<String, ExecutionPlan>> tenantEntry : map.entrySet()) {
            for (Map.Entry<String, ExecutionPlan> executionPlanData : tenantEntry.getValue().entrySet()) {
                byte[] snapshotData = snapshotdataList.get(tenantEntry.getKey()).get(executionPlanData.getKey());
                executionPlanData.getValue().getExecutionPlanRuntime().restore(snapshotData);
            }
        }
    }

    public void pause() {
        readWriteLock.writeLock().lock();
    }

    public void resume() {
        readWriteLock.writeLock().unlock();
    }

    public Lock getReadLock() {
        return readWriteLock.readLock();
    }
}
