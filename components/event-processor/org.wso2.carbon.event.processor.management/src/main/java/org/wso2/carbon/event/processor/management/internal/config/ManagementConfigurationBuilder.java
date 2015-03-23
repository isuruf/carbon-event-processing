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

package org.wso2.carbon.event.processor.management.internal.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.core.exception.DataBridgeConfigurationException;
import org.wso2.carbon.event.processor.common.util.Utils;
import org.wso2.carbon.event.processor.management.config.EventProcessingManagementConfiguration;
import org.wso2.carbon.event.processor.management.config.HAConfiguration;
import org.wso2.carbon.event.processor.management.config.SingleNodeConfiguration;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.net.SocketException;

public class ManagementConfigurationBuilder {
    private static Logger log = Logger.getLogger(ManagementConfigurationBuilder.class);

    public static OMElement loadConfigXML() throws DataBridgeConfigurationException {

        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + ConfigurationConstants.CEP_MANAGEMENT_XML;

        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(path)));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement omElement = builder.getDocumentElement();
            omElement.build();
            return omElement;
        } catch (FileNotFoundException e) {
            String errorMessage = ConfigurationConstants.CEP_MANAGEMENT_XML
                    + "cannot be found in the path : " + path;
            log.error(errorMessage, e);
            throw new DataBridgeConfigurationException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + ConfigurationConstants.CEP_MANAGEMENT_XML
                    + " located in the path : " + path;
            log.error(errorMessage, e);
            throw new DataBridgeConfigurationException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Can not shutdown the input stream";
                log.error(errorMessage, e);
            }
        }
    }

    public static EventProcessingManagementConfiguration getConfiguration() {
        OMElement omElement;
        try {
            omElement = loadConfigXML();
        } catch (DataBridgeConfigurationException e) {
            log.error(e);
            return null;
        }
        String mode;
        OMElement processing = omElement.getFirstChildWithName(
                new QName(ConfigurationConstants.PROCESSING_ELEMENT));
        if (processing == null) {
            log.error("Invalid XML");
            return null;
        }
        mode = processing.getAttribute(new QName(ConfigurationConstants.PROCESSING_MODE_ATTRIBUTE))
                .getAttributeValue();
        if (mode.equals(ConfigurationConstants.PROCESSING_MODE_HA)) {
            return haConfig(processing);
        } else if (mode.equals(ConfigurationConstants.PROCESSING_MODE_SN)) {
            return snConfig(processing);
        }
        return null;
    }

    private static SingleNodeConfiguration snConfig(OMElement processing) {
        SingleNodeConfiguration singleNodeConfiguration = new SingleNodeConfiguration();
        OMElement persistence = processing.getFirstChildWithName(
                new QName(ConfigurationConstants.SN_PERSISTENCE_ELEMENT));
        if (persistence == null) {
            log.warn("Invalid XML. Using default persistence store :"
                    + ConfigurationConstants.SN_DEFAULT_PERSISTENCE_STORE);
            singleNodeConfiguration.setPersistenceClass(ConfigurationConstants.SN_DEFAULT_PERSISTENCE_STORE);
        } else {
            snPopulatePersistenceClass(persistence, singleNodeConfiguration);
        }
        return singleNodeConfiguration;
    }

    private static void snPopulatePersistenceClass(OMElement persistence, SingleNodeConfiguration singleNodeConfiguration) {
        String className = persistence.getAttribute(
                new QName(ConfigurationConstants.SN_PERSISTENCE_CLASS_ATTRIBUTE)).getAttributeValue();
        singleNodeConfiguration.setPersistenceClass(className);
    }

    private static HAConfiguration haConfig(OMElement processing) {
        HAConfiguration haConfiguration = new HAConfiguration();
        OMElement transport = processing.getFirstChildWithName(
                new QName(ConfigurationConstants.HA_TRANSPORT_ELEMENT));
        haConfiguration.setTransport(readHostName(transport),
                readPort(transport, ConfigurationConstants.HA_DEFAULT_TRANSPORT_PORT),
                readReconnectionInterval(transport));

        OMElement management = processing.getFirstChildWithName(
                new QName(ConfigurationConstants.HA_MANAGEMENT_ELEMENT));
        haConfiguration.setManagement(readHostName(management),
                readPort(management, ConfigurationConstants.HA_DEFAULT_MANAGEMENT_PORT));

        return haConfiguration;
    }

    private static String readHostName(OMElement transport) {
        OMElement receiverHostName = transport.getFirstChildWithName(
                new QName(ConfigurationConstants.RECEIVER_HOST_NAME));
        String hostName = null;
        if (receiverHostName != null && receiverHostName.getText() != null
                && !receiverHostName.getText().trim().equals("")) {
            hostName = receiverHostName.getText();
        }
        if (hostName == null) {
            try {
                hostName = Utils.findAddress("localhost");
            } catch (SocketException e) {
                log.error("Unable to find the address of localhost.", e);
            }
        }
        return hostName;
    }

    private static int readPort(OMElement transport, int defaultPort) {
        OMElement receiverPort = transport.getFirstChildWithName(
                new QName(ConfigurationConstants.PORT_ELEMENT));
        int portOffset = haReadPortOffset();
        int port;
        if (receiverPort != null) {
            try {
                return (Integer.parseInt(receiverPort.getText()) + portOffset);
            } catch (NumberFormatException e) {
                port = defaultPort + portOffset;
                log.warn("Invalid port for HA configuration. Using default port " + port, e);
            }
        } else {
            port = defaultPort + portOffset;
            log.warn("Missing port for HA configuration. Using default port" + port);
        }
        return port;
    }

    private static int readReconnectionInterval(OMElement transport) {
        OMElement reconnectionInterval = transport.getFirstChildWithName(
                new QName(ConfigurationConstants.HA_RECONNECTION_INTERVAL_ELEMENT));
        int interval;
        if (reconnectionInterval != null && reconnectionInterval.getText() != null
                && !reconnectionInterval.getText().trim().equals("")) {
            try {
                return Integer.parseInt(reconnectionInterval.getText().trim());
            } catch(NumberFormatException e) {
                interval = ConfigurationConstants.HA_DEFAULT_RECONNECTION_INTERVAL;
                log.warn("Invalid reconnection interval for HA configuration. Using default: " + interval, e);
            }
        } else {
            interval = ConfigurationConstants.HA_DEFAULT_RECONNECTION_INTERVAL;
            log.warn("Missing reconnection interval for HA configuration. Using default: " + interval);
        }
        return interval;
    }


    public static int haReadPortOffset() {
        return CarbonUtils.
                getPortFromServerConfig(ConfigurationConstants.CARBON_CONFIG_PORT_OFFSET_NODE) + 1;
    }
}
