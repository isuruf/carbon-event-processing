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

package org.wso2.carbon.event.receiver.core;

import org.wso2.carbon.event.processor.common.util.HostAndPort;

public interface EventReceiverManagementService {
    /**
     * Get the state of the input event adapter service
     * @return state serialised as a byte array
     */
    public byte[] getState();

    /**
     * Synchronize the state of the input event adapter service
     * @param bytes state of an input event adapter service returned by a call to getState()
     */
    public void syncState(byte[] bytes);

    /**
     * Pause the input event adapter service
     */
    public void pause();

    /**
     * Resume the input event adapter service
     */
    public void resume();


    /**
     * Start the input event adapter service
     */
    public void start();

    public void setOtherMember(HostAndPort otherMember);
}
