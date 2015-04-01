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

package org.wso2.carbon.event.processor.common.config;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.log4j.Logger;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;

public class ManagementInfo {

    private Mode mode;
    private boolean isReceiver;
    private boolean isPublisher;
    private boolean isManager;

    private static Logger log = Logger.getLogger(ManagementInfo.class);

    public static OMElement loadConfigXML() throws ManagementConfigurationException {

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
            String errorMessage = ConfigurationConstants.CEP_MANAGEMENT_XML + "cannot be found in the path : " + path;
            throw new ManagementConfigurationException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + ConfigurationConstants.CEP_MANAGEMENT_XML  + " located in the path : " + path;
            throw new ManagementConfigurationException(errorMessage, e);
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

    public ManagementInfo() throws ManagementConfigurationException {
        OMElement omElement = loadConfigXML();
        String attribute;
        OMElement processing = omElement.getFirstChildWithName(
                new QName(ConfigurationConstants.PROCESSING_ELEMENT));
        if (processing == null) {
            throw new ManagementConfigurationException("Invalid XML. No element with name "  + ConfigurationConstants.PROCESSING_ELEMENT + " found.");
        }
        if (processing.getAttribute(new QName(ConfigurationConstants.PROCESSING_MODE_ATTRIBUTE)) == null) {
            throw new ManagementConfigurationException("Invalid XML. No attribute with name "   + ConfigurationConstants.PROCESSING_MODE_ATTRIBUTE + " found.");
        }
        attribute = processing.getAttribute(new QName(ConfigurationConstants.PROCESSING_MODE_ATTRIBUTE))
                .getAttributeValue();
        if (attribute.equalsIgnoreCase(ConfigurationConstants.PROCESSING_MODE_HA)) {
            mode = Mode.HA;
        } else if (attribute.equalsIgnoreCase(ConfigurationConstants.PROCESSING_MODE_SN)) {
            mode = Mode.SingleNode;
        } else if (attribute.equalsIgnoreCase(ConfigurationConstants.PROCESSING_MODE_DISTRIBUTED)) {
            mode = Mode.Distributed;
            OMElement nodeConfig = omElement.getFirstChildWithName(
                    new QName(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_ELEMENT));
            if (nodeConfig == null) {
                throw new ManagementConfigurationException("Invalid XML. No element with name " + ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_ELEMENT + " found.");
            }
            isReceiver = nodeType(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_RECEIVER_ELEMENT, nodeConfig);
            isPublisher = nodeType(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_PUBLISHER_ELEMENT, nodeConfig);
            isManager = nodeType(ConfigurationConstants.DISTRIBUTED_NODE_CONFIG_MANAGER_ELEMENT, nodeConfig);
        }
    }

    private Boolean nodeType(String elementName, OMElement element) throws ManagementConfigurationException {
        element = element.getFirstChildWithName(new QName(elementName));
        if (element != null) {
            OMAttribute attribute = element.getAttribute(new QName(ConfigurationConstants.PROCESSING_MODE_ATTRIBUTE));
            if (attribute != null) {
                return attribute.getAttributeValue().equalsIgnoreCase("True");
            } else {
                throw new ManagementConfigurationException("Invalid XML. No attribute with name " + ConfigurationConstants.PROCESSING_MODE_ATTRIBUTE + " found.");
            }
        } else {
            throw new ManagementConfigurationException("Invalid XML. No element with name " + elementName + " found.");
        }
    }

    public boolean isReceiver() {
        return isReceiver;
    }

    public boolean isPublisher() {
        return isPublisher;
    }

    public boolean isManager() {
        return isManager;
    }

    public Mode getMode() {
        return mode;
    }
}
