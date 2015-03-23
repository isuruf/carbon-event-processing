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
package org.wso2.carbon.event.processor.management.config;

import org.wso2.carbon.event.processor.core.CEPMembership;
import org.wso2.carbon.event.processor.management.EventProcessingManagement.Mode;

public class HAConfiguration implements EventProcessingManagementConfiguration {
    private CEPMembership management;
    private CEPMembership transport;
    private int reconnectionInterval;

    public HAConfiguration() {
    }

    public int getReconnectionInterval() {
        return reconnectionInterval;
    }

    public void setReconnectionInterval(int reconnectionInterval) {
        this.reconnectionInterval = reconnectionInterval;
    }

    public CEPMembership getManagement() {
        return management;
    }

    public CEPMembership getTransport() {
        return transport;
    }

    public void setManagement(String host, int port) {
        this.management = new CEPMembership(host, port);
    }

    public void setTransport(String host, int port, int reconnectionInterval) {
        this.transport = new CEPMembership(host, port);
    }


    @Override
    public Mode getMode() {
        return Mode.HA;
    }
}
