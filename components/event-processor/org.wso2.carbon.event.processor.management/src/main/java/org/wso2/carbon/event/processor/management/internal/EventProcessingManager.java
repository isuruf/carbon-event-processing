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

import com.hazelcast.core.HazelcastInstance;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.common.config.ManagementConfigurationException;
import org.wso2.carbon.event.processor.common.config.Mode;
import org.wso2.carbon.event.processor.core.EventProcessorManagementService;
import org.wso2.carbon.event.processor.management.internal.config.EventProcessingManagementConfiguration;
import org.wso2.carbon.event.processor.management.internal.config.HAConfiguration;
import org.wso2.carbon.event.processor.management.internal.config.ManagementConfigurationBuilder;
import org.wso2.carbon.event.processor.management.internal.ds.EventProcessingManagementValueHolder;
import org.wso2.carbon.event.receiver.core.EventReceiverManagementService;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class EventProcessingManager {

    private static Logger log = Logger.getLogger(EventProcessingManager.class);

    public ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Mode mode = Mode.SingleNode;
    private HAManager haManager = null;
    private EventProcessingManagementConfiguration eventProcessingManagementConfiguration;

    public EventProcessingManager() {
        try {
            eventProcessingManagementConfiguration = ManagementConfigurationBuilder.getConfiguration();
        } catch (ManagementConfigurationException e) {
            //TODO
        }
        if (eventProcessingManagementConfiguration != null) {
            mode = eventProcessingManagementConfiguration.getMode();
        }
    }

    public void init(HazelcastInstance hazelcastInstance) {
        if (mode == Mode.HA) {
            HAConfiguration haConfiguration = (HAConfiguration) eventProcessingManagementConfiguration;
            haManager = new HAManager(EventProcessingManagementValueHolder.getHazelcastInstance(),
                    haConfiguration, readWriteLock.writeLock(), this);
        } else if (mode == Mode.SingleNode) {
            log.warn("CEP started with clustering enabled, but SingleNode configuration given.");
        } else {
            // Distributed
        }

        if (haManager != null) {
            haManager.init();
        }
    }

    public EventProcessingManagementConfiguration getConfiguration() {
        return eventProcessingManagementConfiguration;
    }

    public void shutdown() {
        if (haManager != null) {
            haManager.shutdown();
        }
    }

    public byte[] getState() {
        if (mode == Mode.HA) {
            return haManager.getState();
        }
        return null;
    }


    public EventProcessorManagementService getEventProcessorManagementService() {
        return EventProcessingManagementValueHolder.getEventProcessorManagementService();
    }


    public EventReceiverManagementService getEventReceiverManagementService() {
        return EventProcessingManagementValueHolder.getEventReceiverManagementService();
    }
}
