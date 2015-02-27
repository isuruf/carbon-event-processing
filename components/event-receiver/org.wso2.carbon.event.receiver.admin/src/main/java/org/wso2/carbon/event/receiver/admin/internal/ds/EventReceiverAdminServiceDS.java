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
package org.wso2.carbon.event.receiver.admin.internal.ds;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.receiver.core.EventReceiverService;
import org.wso2.carbon.event.receiver.core.InputEventAdaptorService;


/**
 * This class is used to get the EventReceiver service.
 *
 * @scr.component name="eventReceiverAdmin.component" immediate="true"
 * @scr.reference name="eventReceiverService.service"
 * interface="org.wso2.carbon.event.receiver.core.EventReceiverService" cardinality="1..1"
 * policy="dynamic" bind="setEventReceiverService" unbind="unsetEventReceiverService"
 * @scr.reference name="inputEventAdaptor.service"
 * interface="org.wso2.carbon.event.receiver.core.InputEventAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setInputEventAdaptorService" unbind="unsetInputEventAdaptorService"
 */
public class EventReceiverAdminServiceDS {
    protected void activate(ComponentContext context) {

    }

    protected void setEventReceiverService(EventReceiverService eventReceiverService) {
        EventReceiverAdminServiceValueHolder.registerEventReceiverService(eventReceiverService);
    }

    protected void unsetEventReceiverService(EventReceiverService eventReceiverService) {
        EventReceiverAdminServiceValueHolder.registerEventReceiverService(null);
    }

    protected void setInputEventAdaptorService(InputEventAdaptorService InputEventAdaptorService) {
        EventReceiverAdminServiceValueHolder.registerInputEventAdaptorService(InputEventAdaptorService);
    }

    protected void unsetInputEventAdaptorService(
            InputEventAdaptorService InputEventAdaptorService) {
        EventReceiverAdminServiceValueHolder.registerInputEventAdaptorService(null);
    }
}
