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
package org.wso2.carbon.event.processor.management.ds;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorService;
import org.wso2.carbon.event.processor.core.internal.ha.server.HAManagementServer;
import org.wso2.carbon.event.processor.core.internal.listener.EventStreamListenerImpl;
import org.wso2.carbon.event.processor.core.internal.persistence.FileSystemPersistenceStore;
import org.wso2.carbon.event.processor.core.internal.storm.manager.StormManagerServer;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.processor.management.EventProcessingManagementService;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.utils.ConfigurationContextService;

//import org.wso2.carbon.cassandra.dataaccess.DataAccessService;

/**
 * @scr.component name="eventProcessorManagementService.component" immediate="true"
 * @scr.reference name-"eventProcessorManagementListener.service"
 * interface="org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory" cardinality="0..n"
 * policy="dynamic" bind="setEventAdapterType" unbind="unSetEventAdapterType"
 * @scr.reference name="eventStatistics.service"
 * interface="org.wso2.carbon.event.statistics.EventStatisticsService" cardinality="1..1"
 * policy="dynamic" bind="setEventStatisticsService" unbind="unsetEventStatisticsService"
 * @scr.reference name="stream.definitionStore.service"
 * interface="org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamStoreService" unbind="unsetEventStreamStoreService"
 * @scr.reference name="eventStreamManager.service"
 * interface="org.wso2.carbon.event.stream.manager.core.EventStreamService" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamManagerService" unbind="unsetEventStreamManagerService"
 * @scr.reference name="hazelcast.instance.service"
 * interface="com.hazelcast.core.HazelcastInstance" cardinality="0..1"
 * policy="dynamic" bind="setHazelcastInstance" unbind="unsetHazelcastInstance"
 * @scr.reference name="user.realm.delegating" interface="org.wso2.carbon.user.core.UserRealm"
 * cardinality="1..1" policy="dynamic" bind="setUserRealm" unbind="unsetUserRealm"
 * @scr.reference name="org.wso2.carbon.ndatasource" interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1" policy="dynamic" bind="setDataSourceService" unbind="unsetDataSourceService"
 * @scr.reference name="server.configuration"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic"  bind="setServerConfiguration" unbind="unsetServerConfiguration"
 * @scr.reference name="configuration.context"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="0..1" policy="dynamic"  bind="setConfigurationContext" unbind="unsetConfigurationContext"
 */
public class EventProcessorManagementServiceDS {
    private static final Log log = LogFactory.getLog(EventProcessorManagementServiceDS.class);

    protected void activate(ComponentContext context) {
        try {

            EventProcessingManagementService eventProcessingManagementService = new EventProcessingManagementService();
            EventProcessorManagementValueHolder.registerEventProcessorService(eventProcessingManagementService);

            /*
            new HAManagementServer(carbonEventProcessorService);

            String stormConfigDirPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "cep" + File.separator + "storm";
            StormDeploymentConfig stormDeploymentConfig = StormDeploymentConfigReader.loadConfigurations(stormConfigDirPath);
            if (stormDeploymentConfig != null) {
                EventProcessorManagementValueHolder.registerStormDeploymentConfig(stormDeploymentConfig);
                if (stormDeploymentConfig.isManagerNode()) {
                    StormManagerServer stormManagerServer = new StormManagerServer(stormDeploymentConfig.getLocalManagerConfig().getHostName(), stormDeploymentConfig.getLocalManagerConfig().getPort());
                    EventProcessorManagementValueHolder.registerStormManagerServer(stormManagerServer);
                }
            }



            context.getBundleContext().registerService(EventProcessorService.class.getName(), carbonEventProcessorService, null);
            EventProcessorManagementValueHolder.getEventStreamService().registerEventStreamListener(new EventStreamListenerImpl());

            SiddhiManager siddhiManager = new SiddhiManager();
            EventProcessorManagementValueHolder.registerSiddhiManager(siddhiManager);

            // TODO: Get the class of the PersistenceStore from a configuration file
            PersistenceStore persistenceStore = new FileSystemPersistenceStore();
            EventProcessorManagementValueHolder.setPersistenceStore(persistenceStore);
            siddhiManager.setPersistenceStore(persistenceStore);
            // TODO: Get the pool size from a configuration file
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
            EventProcessorManagementValueHolder.setScheduledExecutorService(scheduledExecutorService);

            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventProcessorService");
            }*/

        } catch (Throwable e) {
            log.error("Could not create EventProcessorService: " + e.getMessage(), e);
        }

    }

    protected void deactivate(ComponentContext context) {
/*
        try {
            StormManagerServer stormManagerServer = EventProcessorManagementValueHolder.getStormManagerServer();
            if (stormManagerServer != null) {
                stormManagerServer.stop();
            }
            EventProcessorManagementValueHolder.getScheduledExecutorService().shutdownNow();
            EventProcessorManagementValueHolder.getEventProcessorService().shutdown();
        } catch (RuntimeException e) {
            log.error("Error in stopping Storm Manager Service : " + e.getMessage(), e);
        }
*/
    }

    protected void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventProcessorManagementValueHolder.registerEventStatisticsService(eventStatisticsService);
    }

    protected void unsetEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventProcessorManagementValueHolder.registerEventStatisticsService(null);
    }

    protected void setEventStreamStoreService(AbstractStreamDefinitionStore streamDefinitionStore) {
        EventProcessorManagementValueHolder.registerStreamDefinitionStore(streamDefinitionStore);
    }

    protected void unsetEventStreamStoreService(AbstractStreamDefinitionStore streamDefinitionStore) {
        EventProcessorManagementValueHolder.registerStreamDefinitionStore(null);
    }

    protected void setEventStreamManagerService(EventStreamService eventStreamService) {
        EventProcessorManagementValueHolder.registerEventStreamManagerService(eventStreamService);
    }

    protected void unsetEventStreamManagerService(EventStreamService eventStreamService) {
        EventProcessorManagementValueHolder.registerEventStreamManagerService(null);
    }

    protected void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        EventProcessorManagementValueHolder.registerHazelcastInstance(hazelcastInstance);
/*
        StormManagerServer stormManagerServer = EventProcessorManagementValueHolder.getStormManagerServer();
        if (stormManagerServer != null) {
            stormManagerServer.tryBecomeCoordinator();
        }
*/
        hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {
            @Override
            public void memberAdded(MembershipEvent membershipEvent) {

            }

            @Override
            public void memberRemoved(MembershipEvent membershipEvent) {

            }

            @Override
            public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {

            }

        });

        EventProcessorManagementValueHolder.getEventProcessorService().notifyServiceAvailability(EventProcessorConstants.HAZELCAST_INSTANCE);
    }

    protected void unsetHazelcastInstance(HazelcastInstance hazelcastInstance) {
        EventProcessorManagementValueHolder.registerHazelcastInstance(null);
    }

    protected void setUserRealm(UserRealm userRealm) {
        EventProcessorManagementValueHolder.setUserRealm(userRealm);
    }

    protected void unsetUserRealm(UserRealm userRealm) {
        EventProcessorManagementValueHolder.setUserRealm(null);
    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        EventProcessorManagementValueHolder.setDataSourceService(dataSourceService);
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        EventProcessorManagementValueHolder.setDataSourceService(null);
    }

    protected void setServerConfiguration(ServerConfigurationService serverConfiguration) {
        EventProcessorManagementValueHolder.setServerConfiguration(serverConfiguration);
    }

    protected void unsetServerConfiguration(ServerConfigurationService serverConfiguration) {
        EventProcessorManagementValueHolder.setServerConfiguration(null);
    }

    protected void setConfigurationContext(ConfigurationContextService configurationContext) {
        EventProcessorManagementValueHolder.setConfigurationContext(configurationContext);
    }

    protected void unsetConfigurationContext(ConfigurationContextService configurationContext) {
        EventProcessorManagementValueHolder.setConfigurationContext(null);
    }
}
