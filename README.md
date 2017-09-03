# OkHttp AWSv4 Signer interceptor

  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.esiqveland.awssigner/okhttp-awssigner-interceptor/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.esiqveland.awssigner/okhttp-awssigner-interceptor/)
  [![Build Status](https://circleci.com/gh/esiqveland/okhttp-awssigner.svg?&style=shield)](https://circleci.com/gh/esiqveland/okhttp-awssigner)

## What is it?

An interceptor for the nice OkHttpClient from Square to sign requests for use with AWS services that require signatures on requests.

This project aims to follow the v4 signature spec described here: https://docs.aws.amazon.com/general/latest/gr/signature-version-4.html


## Motivation

I could not find a signing interceptor that did not depend on the entire AWS SDK, so I made this one.
My goal is to reduce the number of dependencies, so as to make it very easy to include in any Java project.


## Usage

Interceptor should be included late in the interceptor chain, so that all headers (including `Host`) has been set by OkHttp,
before signing is invoked.

```xml
<dependency>
    <groupId>com.github.esiqveland.awssigner</groupId>
    <artifactId>okhttp-awssigner-interceptor</artifactId>
    <version>0.1</version>
</dependency>
```


```java

String accessKey = "AKIDEXAMPLE";
String secretKey = "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY";
String regionName = "us-east-1";
String serviceName = "iam";


AwsConfiguration cfg = new AwsConfiguration(
        accessKey,
        secretKey,
        regionName,
        serviceName
);


Interceptor awsInterceptor = new AwsSigningInterceptor(cfg);


OkHttpClient client = new OkHttpClient.Builder()
    // NetworkInterceptor is invoked after Host header is set by OkHttpClient, so use this
    .addNetworkInterceptor(awsInterceptor)
    .build();

```

## Credits

The official AWSv4 signature documentation.

Apache [jclouds](https://github.com/jclouds/jclouds) for query parameter parsing and sorting.

## License

Copyright (c) 2017 Eivind Larsen

This library is licensed under the Apache License, Version 2.0.

See http://www.apache.org/licenses/LICENSE-2.0.html or the LICENSE file in this repository for the full license text.

