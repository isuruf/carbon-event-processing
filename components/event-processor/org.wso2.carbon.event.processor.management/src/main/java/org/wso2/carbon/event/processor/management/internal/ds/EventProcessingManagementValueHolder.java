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
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterManagementService;
import org.wso2.carbon.event.processor.core.EventProcessorManagementService;
import org.wso2.carbon.event.processor.core.CEPMembership;
import org.wso2.carbon.event.processor.management.internal.EventProcessingManager;

public class EventProcessingManagementValueHolder {
    private static EventProcessingManager eventProcessingManager;
    private static HazelcastInstance hazelcastInstance;
    private static EventProcessorManagementService eventProcessorManagementService;
    private static InputEventAdapterManagementService inputEventAdapterManagementService;
    private static CEPMembership currentCEPMembershipInfo;

    public static CEPMembership getCurrentCEPMembershipInfo() {
        return currentCEPMembershipInfo;
    }

    public static void setCurrentCEPMembershipInfo(CEPMembership currentCEPMembershipInfo) {
        EventProcessingManagementValueHolder.currentCEPMembershipInfo = currentCEPMembershipInfo;
    }

    public static EventProcessingManager getEventProcessingManager() {
        return eventProcessingManager;
    }

    public static void setEventProcessingManager(EventProcessingManager eventProcessingManager) {
        EventProcessingManagementValueHolder.eventProcessingManager = eventProcessingManager;
    }

    public static void registerHazelcastInstance(HazelcastInstance hazelcastInstance) {
        EventProcessingManagementValueHolder.hazelcastInstance = hazelcastInstance;
    }

    public static HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    public static EventProcessorManagementService getEventProcessorManagementService() {
        return eventProcessorManagementService;
    }

    public static void registerEventProcessorManagementService(EventProcessorManagementService eventProcessorManagementService) {
        EventProcessingManagementValueHolder.eventProcessorManagementService = eventProcessorManagementService;
    }

    public static void registerInputEventAdapterManagementService(InputEventAdapterManagementService inputEventAdapterManagementService) {
        EventProcessingManagementValueHolder.inputEventAdapterManagementService = inputEventAdapterManagementService;
    }
}
