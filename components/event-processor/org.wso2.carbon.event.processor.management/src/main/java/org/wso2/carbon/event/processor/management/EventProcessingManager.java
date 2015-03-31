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

package org.wso2.carbon.event.processor.management;

import com.hazelcast.core.HazelcastInstance;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.common.config.ManagementConfigurationException;
import org.wso2.carbon.event.processor.common.config.Mode;
import org.wso2.carbon.event.processor.core.EventProcessorManagementService;
import org.wso2.carbon.event.processor.management.internal.HAManager;
import org.wso2.carbon.event.processor.management.internal.config.EventProcessingManagementConfiguration;
import org.wso2.carbon.event.processor.management.internal.config.HAConfiguration;
import org.wso2.carbon.event.processor.management.internal.config.ManagementConfigurationBuilder;
import org.wso2.carbon.event.processor.management.internal.ds.EventProcessingManagerValueHolder;
import org.wso2.carbon.event.processor.management.internal.util.Constants;
import org.wso2.carbon.event.publisher.core.EventPublisherManagementService;
import org.wso2.carbon.event.receiver.core.EventReceiverManagementService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class EventProcessingManager {

    private static Logger log = Logger.getLogger(EventProcessingManager.class);

    public ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Mode mode = Mode.SingleNode;
    private HAManager haManager = null;
    private EventProcessingManagementConfiguration eventProcessingManagementConfiguration;
    private ScheduledExecutorService executorService;

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
            haManager = new HAManager(hazelcastInstance, haConfiguration, readWriteLock.writeLock(), this);
        } else if (mode == Mode.SingleNode) {
            log.warn("CEP started with clustering enabled, but SingleNode configuration given.");
        } else {
            // TODO: Distributed
        }

        if (haManager != null) {
            haManager.init();
        }
    }

    public void init(ConfigurationContextService configurationContextService) {
        executorService = new ScheduledThreadPoolExecutor(1);
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                log.info("Starting polling event adapters");
                getEventReceiverManagementService().startPolling();
            }
        }, Constants.AXIS_TIME_INTERVAL_IN_MILLISECONDS * 4, TimeUnit.MILLISECONDS);
    }

    public EventProcessingManagementConfiguration getConfiguration() {
        return eventProcessingManagementConfiguration;
    }

    public void shutdown() {
        if (haManager != null) {
            haManager.shutdown();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public byte[] getState() {
        if (mode == Mode.HA) {
            return haManager.getState();
        }
        return null;
    }


    public EventProcessorManagementService getEventProcessorManagementService() {
        return EventProcessingManagerValueHolder.getEventProcessorManagementService();
    }


    public EventReceiverManagementService getEventReceiverManagementService() {
        return EventProcessingManagerValueHolder.getEventReceiverManagementService();
    }

    public EventPublisherManagementService getEventPublisherManagementService() {
        return EventProcessingManagerValueHolder.getEventPublisherManagementService();
    }
}
