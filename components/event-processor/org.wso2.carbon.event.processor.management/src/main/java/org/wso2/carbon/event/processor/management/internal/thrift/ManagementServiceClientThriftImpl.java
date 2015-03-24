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
package org.wso2.carbon.event.processor.management.internal.thrift;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.event.processor.core.CEPMembership;
import org.wso2.carbon.event.processor.management.internal.ManagementServiceClient;
import org.wso2.carbon.event.processor.management.internal.thrift.exception.InternalServerException;
import org.wso2.carbon.event.processor.management.internal.thrift.service.ManagementService;

public class ManagementServiceClientThriftImpl implements ManagementServiceClient {

    private static final Log log = LogFactory.getLog(ManagementServiceClientThriftImpl.class);

    @Override
    public byte[] getSnapshot(CEPMembership activeMember) {

        TTransport receiverTransport = null;
        receiverTransport = new TSocket((activeMember.getHost()), activeMember.getPort());
        TProtocol protocol = new TBinaryProtocol(receiverTransport);
        ManagementService.Client client = new ManagementService.Client(protocol);
        try {
            receiverTransport.open();
        } catch (TTransportException e) {
            throw new RuntimeException("Error in connecting to " + activeMember.getHost() + ":" + activeMember.getPort());
        }

        try {
            log.info("Requesting snapshot from " + activeMember.getHost() + ":" + activeMember.getPort());

            org.wso2.carbon.event.processor.management.internal.thrift.data.SnapshotData snapshotDataIn = client.takeSnapshot();
            log.info("Snapshot received.");

            return snapshotDataIn.getState();

        } catch (InternalServerException e) {
            throw new RuntimeException("Internal server error occurred at CEP member :" + activeMember.getHost() + ":" + activeMember.getPort() + ", " + e.getMessage());
        } catch (TException e) {
            throw new RuntimeException("Thrift error occurred when communicating to CEP member :" + activeMember.getHost() + ":" + activeMember.getPort() + ", " + e.getMessage());
        }
    }
}