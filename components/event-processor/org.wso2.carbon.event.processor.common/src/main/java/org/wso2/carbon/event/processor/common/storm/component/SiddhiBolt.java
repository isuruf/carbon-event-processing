/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.event.processor.common.storm.component;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.common.util.Utils;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Bold which runs Siddhi engine
 */

public class SiddhiBolt extends BaseBasicBolt {
    private final String name;
    private transient Logger log = Logger.getLogger(SiddhiBolt.class);
    private transient SiddhiManager siddhiManager;

    /**
     * Exported stream IDs. Must declare output filed for each exported stream
     */
    private List<String> outputStreamDefinitions;
    /**
     * All stream inputStreamDefinitions and partition inputStreamDefinitions(if any)
     */
    private List<String> inputStreamDefinitions;
    /**
     * Queries to be executed in Siddhi.
     */
    private String query;

    private BasicOutputCollector collector;
    private int eventCount;
    private long batchStartTime;
    private String logPrefix;

    private transient ExecutionPlanRuntime executionPlanRuntime;

    /**
     * Bolt which runs the Siddhi engine.
     *
     * @param inputStreamDefinitions  - All stream and partition inputStreamDefinitions
     * @param query                   - Siddhi query
     * @param outputSiddhiDefinitions
     */
    public SiddhiBolt(String name, List<String> inputStreamDefinitions, String query,
                      List<String> outputSiddhiDefinitions, String executionPlanName, int tenantId) {
        this.inputStreamDefinitions = inputStreamDefinitions;
        this.query = query;
        this.outputStreamDefinitions = outputSiddhiDefinitions;
        this.name = name;
        init();
        this.logPrefix = "[" + tenantId + ":" + executionPlanName + ":" + name + "]";
    }

    /**
     * Bolt get saved and reloaded, this to redo the configurations.
     */
    private void init() {
        siddhiManager = new SiddhiManager();
        eventCount = 0;
        batchStartTime = System.currentTimeMillis();
        log = Logger.getLogger(SiddhiBolt.class);

        String fullQueryExpression = Utils.constructQueryExpression(inputStreamDefinitions, outputStreamDefinitions,
                query);
        executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(fullQueryExpression);

        for (String outputStreamDefinition : outputStreamDefinitions) {
            final StreamDefinition outputSiddhiDefinition = SiddhiCompiler.parseStreamDefinition
                    (outputStreamDefinition);
            log.info(logPrefix +" Adding callback for stream: " + outputSiddhiDefinition.getId());
            executionPlanRuntime.addCallback(outputSiddhiDefinition.getId(), new StreamCallback() {

                @Override
                public void receive(Event[] events) {
                    for (Event event : events) {
                        collector.emit(outputSiddhiDefinition.getId(), Arrays.asList(event.getData()));
                        if (log.isDebugEnabled()) {
                            if (++eventCount % 10000 == 0) {
                                double timeSpentInSecs = (System.currentTimeMillis() - batchStartTime) / 1000.0D;
                                double throughput = 10000 / timeSpentInSecs;
                                log.debug(logPrefix + "Processed 10000 events in " + timeSpentInSecs + " seconds, " +
                                        "throughput : " + throughput + " events/sec. Stream :" +
                                        outputSiddhiDefinition.getId());
                                eventCount = 0;
                                batchStartTime = System.currentTimeMillis();
                            }
                            log.debug(logPrefix + "Emitted Event:" + outputSiddhiDefinition.getId() +
                                    ":" + event.toString());
                        }
                    }
                }
            });
            executionPlanRuntime.start();
        }
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        if (siddhiManager == null) {
            init();
        }

        try {
            this.collector = collector;
            InputHandler inputHandler = executionPlanRuntime.getInputHandler(tuple.getSourceStreamId());
            if (log.isDebugEnabled()) {
                log.debug(logPrefix + "Received Event: " + tuple.getSourceStreamId() + ":" + Arrays.deepToString
                        (tuple.getValues().toArray()));
            }

            if (inputHandler != null) {
                inputHandler.send(tuple.getValues().toArray());
            } else {
                log.warn(logPrefix + "Event received for unknown stream " + tuple.getSourceStreamId() + ". Discarding" +
                        " the event :" + Arrays.deepToString(tuple.getValues().toArray()));
            }
        } catch (InterruptedException e) {
            log.error(e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        if (siddhiManager == null) {
            init();
        }

        // Declaring output fields for each exported stream ID
        for (String outputStreamDefinition : outputStreamDefinitions) {
            StreamDefinition siddhiOutputDefinition = SiddhiCompiler.parseStreamDefinition(outputStreamDefinition);
            if (outputStreamDefinition == null) {
                throw new RuntimeException(logPrefix + "Cannot find exported stream : " + siddhiOutputDefinition.getId
                        ());
            }
            List<String> list = new ArrayList<String>();

            for (Attribute attribute : siddhiOutputDefinition.getAttributeList()) {
                list.add(attribute.getName());
            }
            Fields fields = new Fields(list);
            declarer.declareStream(siddhiOutputDefinition.getId(), fields);
            log.info(logPrefix + "Declaring output field for stream :" + siddhiOutputDefinition.getId());
        }
    }
}
