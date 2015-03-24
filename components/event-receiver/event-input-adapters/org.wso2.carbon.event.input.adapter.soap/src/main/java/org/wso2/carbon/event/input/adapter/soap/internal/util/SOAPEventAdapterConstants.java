/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.input.adapter.soap.internal.util;


public final class SOAPEventAdapterConstants {

    private SOAPEventAdapterConstants() {
    }

    public static final String ADAPTER_TYPE_SOAP = "soap";
    public static final String ADAPTER_MESSAGE_OPERATION_NAME = "operation";
    public static final String ADAPTER_MESSAGE_HINT_OPERATION_NAME = "operation.hint";
    

    public static final int ADAPTER_MIN_THREAD_POOL_SIZE = 8;
    public static final int ADAPTER_MAX_THREAD_POOL_SIZE = 100;
    public static final int ADAPTER_EXECUTOR_JOB_QUEUE_SIZE = 10000;
    public static final long DEFAULT_KEEP_ALIVE_TIME = 20;
    public static final String ENDPOINT_PREFIX = "/endpoints/";
    public static final String SEPARATOR = "/";
    public static final String ENDPOINT_TENANT_KEY = "t";


}
