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

package org.wso2.carbon.event.input.adapter.core.internal;

import org.apache.log4j.Logger;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterManagementService;
import org.wso2.carbon.event.input.adapter.core.internal.ds.InputEventAdapterServiceValueHolder;
import org.wso2.carbon.event.input.adapter.core.internal.management.ByteSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CarbonInputEventAdapterManagementService implements InputEventAdapterManagementService {

    private Logger log = Logger.getLogger(CarbonInputEventAdapterManagementService.class);
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    @Override
    public byte[] getState() {
        Map<Integer, ConcurrentHashMap<String, InputAdapterRuntime>> tenantSpecificEventAdapters = InputEventAdapterServiceValueHolder.getCarbonInputEventAdapterService().getTenantSpecificEventAdapters();
        Map<Integer, HashMap<String, byte[]>> data = new HashMap<Integer, HashMap<String, byte[]>>();
        for (Map.Entry<Integer, ConcurrentHashMap<String, InputAdapterRuntime>> pair : tenantSpecificEventAdapters.entrySet()) {
            Map<String, InputAdapterRuntime> map = pair.getValue();
            int tenantId = pair.getKey();
            HashMap<String, byte[]> tenantData = new HashMap<String, byte[]>();

            for (Map.Entry<String, InputAdapterRuntime> adapterRuntimeEntry : map.entrySet()) {
                byte[] state = adapterRuntimeEntry.getValue().getInputEventDispatcher().getState();
                if (state != null) {
                    tenantData.put(adapterRuntimeEntry.getKey(), state);
                }
            }
            data.put(tenantId, tenantData);
        }
        return ByteSerializer.OToB(data);
    }

    @Override
    public void syncState(byte[] bytes) {
        Map<Integer, ConcurrentHashMap<String, InputAdapterRuntime>> tenantSpecificEventAdapters = InputEventAdapterServiceValueHolder.getCarbonInputEventAdapterService().getTenantSpecificEventAdapters();
        Map<Integer, HashMap<String, byte[]>> data = (Map<Integer, HashMap<String, byte[]>>) ByteSerializer.BToO(bytes);
        for (Map.Entry<Integer, HashMap<String, byte[]>> pair : data.entrySet()) {
            Map<String, byte[]> map = pair.getValue();
            int tenantId = pair.getKey();
            for (Map.Entry<String, byte[]> adapterRuntimeEntry : map.entrySet()) {
                tenantSpecificEventAdapters.get(tenantId).get(adapterRuntimeEntry.getKey())
                        .getInputEventDispatcher().syncState(adapterRuntimeEntry.getValue());
            }
        }
    }

    @Override
    public void pause() {
        readWriteLock.writeLock().lock();
    }

    @Override
    public void resume() {
        InputEventAdapterServiceValueHolder.getCarbonInputEventAdapterService().startInputEventAdapters();
        readWriteLock.writeLock().unlock();
    }

    public Lock getReadLock() {
        return readWriteLock.readLock();
    }
}
