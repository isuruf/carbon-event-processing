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
package org.wso2.carbon.event.processor.management.internal.server;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorService;
import org.wso2.carbon.event.processor.management.internal.thrift.service.HAManagementService;
import org.wso2.carbon.event.processor.core.CEPMembership;
import org.wso2.carbon.event.processor.management.EventProcessingManagement.Mode;
import org.wso2.carbon.event.processor.management.config.EventProcessingManagementConfiguration;
import org.wso2.carbon.event.processor.management.config.HAConfiguration;

import java.net.InetSocketAddress;

public class HAManagementServer {
    private static final Log log = LogFactory.getLog(HAManagementServer.class);

    public static void start(EventProcessingManagementConfiguration config) {

        try {
            if (config.getMode() == Mode.HA) {
                HAConfiguration haConfiguration = (HAConfiguration) config;
                carbonEventProcessorService.addCurrentCEPMembership(haConfiguration.getManagement());
                start(haConfiguration.getManagement(), carbonEventProcessorService);
            }
        } catch (RuntimeException e) {
            log.error("Error in starting Agent Server ", e);
        } catch (Throwable e) {
            log.error("Error in starting Agent Server ", e);
        }
    }

    private static void start(CEPMembership cepMembership, CarbonEventProcessorService carbonEventProcessorService) throws Exception {
        try {
            TServerSocket serverTransport = new TServerSocket(
                    new InetSocketAddress(cepMembership.getHost(), cepMembership.getPort()));
            HAManagementService.Processor<HAManagementServiceImpl> processor =
                    new HAManagementService.Processor<HAManagementServiceImpl>(
                            new HAManagementServiceImpl(carbonEventProcessorService));
            TThreadPoolServer dataReceiverServer = new TThreadPoolServer(
                    new TThreadPoolServer.Args(serverTransport).processor(processor));
            Thread thread = new Thread(new ServerThread(dataReceiverServer));
            log.info("CEP HA Management Thrift Server started on " + cepMembership.getHost() + ":" + cepMembership.getPort());
            thread.start();
        } catch (TTransportException e) {
            throw new Exception("Cannot start CEP HA Management Thrift server on port " +  cepMembership.getPort() +
                    " on host " + cepMembership.getHost(), e);
        }
    }

    static class ServerThread implements Runnable {
        private TServer server;

        ServerThread(TServer server) {
            this.server = server;
        }

        public void run() {
            this.server.serve();
        }
    }

}
