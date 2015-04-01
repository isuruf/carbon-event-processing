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
package org.wso2.carbon.event.processor.common.storm.config;

import org.wso2.carbon.event.processor.common.util.HostAndPort;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StormDeploymentConfig implements Serializable {

    private boolean receiverNode = true;
    private boolean publisherNode = true;
    private boolean managerNode = true;
    private HostAndPort localManagerConfig = new HostAndPort("localhost", 8904);

    private List<HostAndPort> managers = new ArrayList<HostAndPort>();


    private int transportMaxPort = 15100;
    private int transportMinPort = 15000;
    private int transportReconnectInterval = 15000;
    private String jar;

    private int topologySubmitRetryInterval = 10000;
    private int heartbeatInterval = 5000;
    private int managementReconnectInterval = 10000;

    private int receiverSpoutParallelism = 1;
    private int publisherBoltParallelism = 1;

    private String distributedUIUrl;


    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public int getTopologySubmitRetryInterval() {
        return topologySubmitRetryInterval;
    }

    public void setTopologySubmitRetryInterval(int topologySubmitRetryInterval) {
        this.topologySubmitRetryInterval = topologySubmitRetryInterval;
    }

    public boolean isReceiverNode() {
        return receiverNode;
    }

    public void setReceiverNode(boolean receiverNode) {
        this.receiverNode = receiverNode;
    }

    public boolean isPublisherNode() {
        return publisherNode;
    }

    public void setPublisherNode(boolean publisherNode) {
        this.publisherNode = publisherNode;
    }

    public boolean isManagerNode() {
        return managerNode;
    }

    public void setManagerNode(boolean managerNode) {
        this.managerNode = managerNode;
    }

    public HostAndPort getLocalManagerConfig() {
        return localManagerConfig;
    }

    public void setLocalManagerConfig(String hostName, int port) {
        this.localManagerConfig = new HostAndPort(hostName, port);
    }

    public List<HostAndPort> getManagers() {
        return managers;
    }

    public void addManager(String hostName, int port) {
        this.managers.add(new HostAndPort(hostName, port));
    }

    public int getManagementReconnectInterval() {
        return managementReconnectInterval;
    }

    public void setManagementReconnectInterval(int managementReconnectInterval) {
        this.managementReconnectInterval = managementReconnectInterval;
    }

    public int getTransportMaxPort() {
        return transportMaxPort;
    }

    public void setTransportMaxPort(int transportMaxPort) {
        this.transportMaxPort = transportMaxPort;
    }

    public int getTransportMinPort() {
        return transportMinPort;
    }

    public void setTransportMinPort(int transportMinPort) {
        this.transportMinPort = transportMinPort;
    }

    public int getTransportReconnectInterval() {
        return transportReconnectInterval;
    }

    public void setTransportReconnectInterval(int transportReconnectInterval) {
        this.transportReconnectInterval = transportReconnectInterval;
    }

    public void setJar(String jar) {
        this.jar = jar;
    }

    public String getJar() {
        return jar;
    }

    public int getPublisherBoltParallelism() {
        return publisherBoltParallelism;
    }

    public void setPublisherBoltParallelism(int publisherBoltParallelism) {
        this.publisherBoltParallelism = publisherBoltParallelism;
    }

    public int getReceiverSpoutParallelism() {
        return receiverSpoutParallelism;
    }

    public void setReceiverSpoutParallelism(int receiverSpoutParallelism) {
        this.receiverSpoutParallelism = receiverSpoutParallelism;
    }

    public String getDistributedUIUrl() {
        return distributedUIUrl;
    }

    public void setDistributedUIUrl(String distributedUIUrl) {
        this.distributedUIUrl = distributedUIUrl;
    }
}
