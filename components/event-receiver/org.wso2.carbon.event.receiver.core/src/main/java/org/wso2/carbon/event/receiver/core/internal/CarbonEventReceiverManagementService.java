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

package org.wso2.carbon.event.receiver.core.internal;

import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.common.transport.client.TCPEventPublisher;
import org.wso2.carbon.event.processor.common.util.HostAndPort;
import org.wso2.carbon.event.receiver.core.EventReceiverManagementService;
import org.wso2.carbon.event.input.adapter.core.internal.ds.InputEventAdapterServiceValueHolder;
import org.wso2.carbon.event.receiver.core.internal.ds.EventReceiverServiceValueHolder;
import org.wso2.carbon.event.receiver.core.internal.management.ByteSerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CarbonEventReceiverManagementService implements EventReceiverManagementService {

    private Logger log = Logger.getLogger(CarbonEventReceiverManagementService.class);
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private HostAndPort otherMember;
    private TCPEventPublisher tcpEventPublisher;

    @Override
    public byte[] getState() {
        Map<Integer, Map<String, EventReceiver>> tenantSpecificEventAdapters = EventReceiverServiceValueHolder.getCarbonEventReceiverService().getTenantSpecificEventReceiverMap();
        Map<Integer, HashMap<String, byte[]>> data = new HashMap<Integer, HashMap<String, byte[]>>();
        for (Map.Entry<Integer, Map<String, EventReceiver>> pair : tenantSpecificEventAdapters.entrySet()) {
            Map<String, EventReceiver> map = pair.getValue();
            int tenantId = pair.getKey();
            HashMap<String, byte[]> tenantData = new HashMap<String, byte[]>();

            for (Map.Entry<String, EventReceiver> receiverEntry : map.entrySet()) {
                byte[] state = receiverEntry.getValue().getInputEventDispatcher().getState();
                if (state != null) {
                    tenantData.put(receiverEntry.getKey(), state);
                }
            }
            data.put(tenantId, tenantData);
        }
        return ByteSerializer.OToB(data);
    }

    @Override
    public void syncState(byte[] bytes) {
        Map<Integer, Map<String, EventReceiver>> tenantSpecificEventAdapters = EventReceiverServiceValueHolder.getCarbonEventReceiverService().getTenantSpecificEventReceiverMap();
        Map<Integer, HashMap<String, byte[]>> data = new HashMap<Integer, HashMap<String, byte[]>>();
        for (Map.Entry<Integer, HashMap<String, byte[]>> pair : data.entrySet()) {
            Map<String, byte[]> map = pair.getValue();
            int tenantId = pair.getKey();
            for (Map.Entry<String, byte[]> receiverEntry : map.entrySet()) {
                tenantSpecificEventAdapters.get(tenantId).get(receiverEntry.getKey())
                        .getInputEventDispatcher().syncState(receiverEntry.getValue());
            }
        }
    }

    @Override
    public void pause() {
        readWriteLock.writeLock().lock();
    }

    @Override
    public void resume() {
        readWriteLock.writeLock().unlock();
    }

    /**
     * Start the input event adapter service
     */
    @Override
    public void start() {
        EventReceiverServiceValueHolder.getCarbonEventReceiverService().startInputAdapterRuntimes();
    }

    @Override
    public void setOtherMember(HostAndPort otherMember) {
        if ((this.otherMember == null || !this.otherMember.equals(otherMember)) &&
                tcpEventPublisher == null) {
            try {
                tcpEventPublisher = new TCPEventPublisher(otherMember.getHostName() + ":" + otherMember.getPort(),
                        false);
            } catch (IOException e) {
                //TODO
            }
        }
        this.otherMember = otherMember;
    }

    public void sendToOther(String streamId, Object[] data) {
        if (tcpEventPublisher != null) {
            try {
                tcpEventPublisher.sendEvent(streamId, data, true);
            } catch (IOException e) {
                //TODO
            }
        }
    }

    public Lock getReadLock() {
        return readWriteLock.readLock();
    }
}
