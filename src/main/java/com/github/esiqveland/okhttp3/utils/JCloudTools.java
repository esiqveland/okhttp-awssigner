/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Parts taken from jclouds project.
 * All code and comments in functions have been kept intact as well as notice.
 */
package com.github.esiqveland.okhttp3.utils;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.google.common.io.BaseEncoding.base16;

public class JCloudTools {
    /**
     * hash input with sha256
     *
     * @param is a stream of bytes to hash
     * @return hash result
     */
    public static byte[] hash(InputStream is) {
        HashingInputStream his = new HashingInputStream(Hashing.sha256(), is);
        try {
            ByteStreams.copy(his, ByteStreams.nullOutputStream());
            return his.hash().asBytes();
        } catch (IOException e) {
            throw new RuntimeException("Unable to compute hash while signing request: " + e.getMessage(), e);
        }
    }


    private final static String EMPTY_HASH = createEmptyPayloadContentHash();

    /**
     * The hash returns the following value: e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
     *
     * @return hash of the empty payload
     */
    public static String getEmptyPayloadContentHash() {
        return EMPTY_HASH;
    }

    private static String createEmptyPayloadContentHash() {
        return base16().lowerCase().encode(hash(new ByteArrayInputStream(new byte[0])));
    }

}
