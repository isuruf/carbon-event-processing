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
import org.wso2.carbon.event.processor.management.EventProcessingManagementService;
import org.wso2.carbon.event.processor.management.internal.CarbonEventProcessingManagementService;

/**
 * @scr.component name="eventProcessorManagementService.component" immediate="true"
 * @scr.reference name="hazelcast.instance.service"
 * interface="com.hazelcast.core.HazelcastInstance" cardinality="0..1"
 * policy="dynamic" bind="setHazelcastInstance" unbind="unsetHazelcastInstance"
 * @scr.reference name="event.processormanagement.service"
 * interface="org.wso2.carbon.event.processor.core.EventProcessorManagementService" cardinality="1..1"
 * policy="dynamic" bind="setEventProcessorManagementService" unbind="unSetEventProcessorManagementService"
 */
public class EventProcessingManagementServiceDS {
    private static final Log log = LogFactory.getLog(EventProcessingManagementServiceDS.class);

    protected void activate(ComponentContext context) {
        try {

            CarbonEventProcessingManagementService eventProcessingManagementService = new CarbonEventProcessingManagementService();
            EventProcessingManagementValueHolder.registerEventProcessingManagementService(eventProcessingManagementService);
            context.getBundleContext().registerService(EventProcessingManagementService.class.getName(), eventProcessingManagementService, null);

            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventProcessorManagementService");
            }

        } catch (Throwable e) {
            log.error("Could not create EventProcessorManagementService: " + e.getMessage(), e);
        }

    }

    protected void deactivate(ComponentContext context) {
        EventProcessingManagementValueHolder.getEventProcessingManagementService().shutdown();
    }

    protected void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        EventProcessingManagementValueHolder.registerHazelcastInstance(hazelcastInstance);

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
        EventProcessingManagementValueHolder.getEventProcessingManagementService().init(hazelcastInstance);
    }

    protected void unsetHazelcastInstance(HazelcastInstance hazelcastInstance) {
        EventProcessingManagementValueHolder.registerHazelcastInstance(null);
    }

    public void setEventProcessorManagementService(EventProcessorManagementService eventProcessorManagementService) {
        EventProcessingManagementValueHolder.registerEventProcessorManagementService(eventProcessorManagementService);
    }

    public void unSetEventProcessorManagementService(EventProcessorManagementService eventProcessorManagementService) {
        EventProcessingManagementValueHolder.registerEventProcessorManagementService(null);
    }
}
