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

package org.wso2.carbon.event.input.adapter.core.internal.management;

import com.hazelcast.nio.serialization.ConstantSerializers;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterSubscription;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;

public class QueueInputEventDispatcher extends AbstractInputEventDispatcher {

    private Logger log = Logger.getLogger(AbstractInputEventDispatcher.class);
    private final BlockingQueue<Object> eventQueue = new LinkedBlockingQueue<Object>();
    private Lock readLock;
    private boolean alive;

    public QueueInputEventDispatcher(InputEventAdapterSubscription inputEventAdapterSubscription, Lock readLock) {
        super(inputEventAdapterSubscription);
        this.readLock = readLock;
        new Thread(new SiddhiProcessInvoker()).run();
        this.alive = true;
    }

    public BlockingQueue<Object> getEventQueue() {
        return eventQueue;
    }

    @Override
    public void onEvent(Object event) {
        try {
            eventQueue.put(event);
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting to put the event to queue.", e);
        }
    }

    @Override
    public void shutdown() {
        alive = false;
    }

    @Override
    public byte[] getState() {
        return ByteSerializer.OToB(eventQueue.toArray());
    }

    @Override
    public void syncState(byte[] bytes) {
        Object[] events = (Object[])ByteSerializer.BToO(bytes);
        for(Object object: events) {
            if(object.equals(eventQueue.peek())) {
                eventQueue.poll();
            } else {
                break;
            }
        }
    }

    class SiddhiProcessInvoker implements Runnable {

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            while (alive) {
                try {
                    readLock.lock();
                    Object event = eventQueue.take();
                    readLock.unlock();
                    inputEventAdapterSubscription.onEvent(event);
                } catch (InterruptedException e) {
                    log.error("Interrupted while waiting to get an event from queue.", e);
                }
            }
        }
    }
}
