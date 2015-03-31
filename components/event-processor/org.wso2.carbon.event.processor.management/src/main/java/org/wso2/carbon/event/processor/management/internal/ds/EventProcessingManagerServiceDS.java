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
package org.wso2.carbon.event.processor.management.internal.ds;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.processor.core.EventProcessorManagementService;
import org.wso2.carbon.event.processor.management.EventProcessingManager;
import org.wso2.carbon.event.publisher.core.EventPublisherManagementService;
import org.wso2.carbon.event.receiver.core.EventReceiverManagementService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="eventProcessorManagementService.component" immediate="true"
 * @scr.reference name="hazelcast.instance.service"
 * interface="com.hazelcast.core.HazelcastInstance" cardinality="0..1"
 * policy="dynamic" bind="setHazelcastInstance" unbind="unsetHazelcastInstance"
 * @scr.reference name="event.processormanagement.service"
 * interface="org.wso2.carbon.event.processor.core.EventProcessorManagementService" cardinality="1..1"
 * policy="dynamic" bind="setEventProcessorManagementService" unbind="unSetEventProcessorManagementService"
 * @scr.reference name="event.eventreceivermanagementservice.service"
 * interface="org.wso2.carbon.event.receiver.core.EventReceiverManagementService" cardinality="1..1"
 * policy="dynamic" bind="setEventReceiverManagementService" unbind="unSetEventReceiverManagementService"
 * @scr.reference name="event.eventpublishermanagementservice.service"
 * interface="org.wso2.carbon.event.publisher.core.EventPublisherManagementService" cardinality="1..1"
 * policy="dynamic" bind="setEventPublisherManagementService" unbind="unSetEventPublisherManagementService"
 * @scr.reference name="configuration.contextService.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="0..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */

public class EventProcessingManagerServiceDS {
    private static final Log log = LogFactory.getLog(EventProcessingManagerServiceDS.class);

    protected void activate(ComponentContext context) {
        try {

            EventProcessingManager eventProcessingManager = new EventProcessingManager();
            EventProcessingManagerValueHolder.setEventProcessingManager(eventProcessingManager);

            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventProcessorManagementService");
            }

        } catch (Throwable e) {
            log.error("Could not create EventProcessorManagementService: " + e.getMessage(), e);
        }

    }

    protected void deactivate(ComponentContext context) {
        EventProcessingManagerValueHolder.getEventProcessingManager().shutdown();
    }

    protected void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        EventProcessingManagerValueHolder.registerHazelcastInstance(hazelcastInstance);

        hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {
            @Override
            public void memberAdded(MembershipEvent membershipEvent) {

            }

            @Override
            public void memberRemoved(MembershipEvent membershipEvent) {

            }

            @Override
            public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {

            }

        });
        EventProcessingManagerValueHolder.getEventProcessingManager().init(hazelcastInstance);
    }

    protected void unsetHazelcastInstance(HazelcastInstance hazelcastInstance) {
        EventProcessingManagerValueHolder.registerHazelcastInstance(null);
    }

    public void setEventProcessorManagementService(EventProcessorManagementService eventProcessorManagementService) {
        EventProcessingManagerValueHolder.registerEventProcessorManagementService(eventProcessorManagementService);
    }

    public void unSetEventProcessorManagementService(EventProcessorManagementService eventProcessorManagementService) {
        EventProcessingManagerValueHolder.registerEventProcessorManagementService(null);
    }

    public void setEventReceiverManagementService(EventReceiverManagementService eventReceiverManagementService) {
        EventProcessingManagerValueHolder.registerEventReceiverManagementService(eventReceiverManagementService);
    }

    public void unSetEventReceiverManagementService(EventReceiverManagementService eventReceiverManagementService) {
        EventProcessingManagerValueHolder.registerEventReceiverManagementService(null);
    }

    public void setEventPublisherManagementService(EventPublisherManagementService eventPublisherManagementService) {
        EventProcessingManagerValueHolder.registerEventPublisherManagementService(eventPublisherManagementService);
    }

    public void unSetEventPublisherManagementService(EventPublisherManagementService eventPublisherManagementService) {
        EventProcessingManagerValueHolder.registerEventPublisherManagementService(null);
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        EventProcessingManagerValueHolder.getEventProcessingManager().init(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
    }
}
