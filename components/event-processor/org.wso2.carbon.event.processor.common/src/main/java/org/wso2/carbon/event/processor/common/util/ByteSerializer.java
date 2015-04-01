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
package org.wso2.carbon.event.processor.common.util;

import org.apache.log4j.Logger;
import java.io.*;


public class ByteSerializer {
    private static final Logger log = Logger.getLogger(ByteSerializer.class);

    static public byte[] OToB(Object obj) {
        long start = System.currentTimeMillis();
        byte[] out = null;
        if (obj != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(obj);
                out = baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        long end = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Encoded in :" + (end - start) + " msec");
        }
        return out;
    }

    static public Object BToO(byte[] bytes) {
        long start = System.currentTimeMillis();
        Object out = null;
        if (bytes != null) {
            try {
                ByteArrayInputStream bios = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bios);
                out = ois.readObject();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
        long end = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Decoded in :" + (end - start) + " msec");
        }
        return out;
    }
}