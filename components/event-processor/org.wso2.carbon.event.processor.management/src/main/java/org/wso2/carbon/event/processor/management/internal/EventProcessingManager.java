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
import org.wso2.carbon.event.processor.core.CEPMembership;
import org.wso2.carbon.event.processor.management.config.EventProcessingManagementConfiguration;
import org.wso2.carbon.event.processor.management.config.HAConfiguration;
import org.wso2.carbon.event.processor.management.internal.config.ManagementConfigurationBuilder;
import org.wso2.carbon.event.processor.management.internal.ds.EventProcessingManagementValueHolder;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class EventProcessingManager {

    public enum Mode {
        SingleNode, HA, Distributed
    }

    private static Logger log = Logger.getLogger(EventProcessingManager.class);

    public ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Mode mode = Mode.SingleNode;
    private HAManager haManager = null;
    private EventProcessingManagementConfiguration eventProcessingManagementConfiguration;

    public EventProcessingManager() {
        eventProcessingManagementConfiguration = ManagementConfigurationBuilder.getConfiguration();
        if (eventProcessingManagementConfiguration != null) {
            mode = eventProcessingManagementConfiguration.getMode();
        }
    }

    public void init(HazelcastInstance hazelcastInstance) {
        if (mode == Mode.HA) {
            HAConfiguration haConfiguration = (HAConfiguration) eventProcessingManagementConfiguration;
            haManager = new HAManager(EventProcessingManagementValueHolder.getHazelcastInstance(),
                    haConfiguration, readWriteLock);
        } else if (mode == Mode.SingleNode) {
            log.warn("CEP started with clustering enabled, but SingleNode configuration given.");
        } else {
            // Distributed
        }

        if (haManager != null) {
            haManager.init();
        }
    }

    public CEPMembership getCurrentCEPMembershipInfo() {
        return EventProcessingManagementValueHolder.getCurrentCEPMembershipInfo();
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
        //Stop receivers etc.
        return EventProcessingManagementValueHolder.getEventProcessorManagementService().getState();
    }
}
