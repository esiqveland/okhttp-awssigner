/**
 * Copyright 2017 Eivind Larsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.esiqveland.awssigner;

public class AwsConfiguration {
    public final String awsAccessKey;
    public final String awsSecretKey;
    public final String awsRegion;
    public final String awsServiceName;

    public AwsConfiguration(String awsAccessKey, String awsSecretKey, String awsRegion, String awsServiceName) {
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
        this.awsRegion = awsRegion;
        this.awsServiceName = awsServiceName;
    }

}
