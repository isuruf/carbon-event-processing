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

package org.wso2.carbon.event.processor.core;

public interface EventProcessorManagementService {
    /**
     * Get the state of the event processor service
     * @return state serialised as a byte array
     */
    public byte[] getState();

    /**
     * Restore the state of the event processor service
     * @param bytes state of an event processor service returned by a call to getState()
     */
    public void restoreState(byte[] bytes);

    /**
     * Try to pause the event processor service
     * @param timeout to return
     */
    public void tryPause(long timeout);

    /**
     * Resume the event processor service
     */
    public void resume();
}
