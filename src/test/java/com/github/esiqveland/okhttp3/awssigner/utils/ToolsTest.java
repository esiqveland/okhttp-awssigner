package com.github.esiqveland.okhttp3.awssigner.utils;

import com.github.esiqveland.okhttp3.awssigner.AwsConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.google.common.io.BaseEncoding.base16;
import static org.assertj.core.api.Assertions.assertThat;

public class ToolsTest {
    private AwsConfiguration cfg;

    @Before
    public void setUp() throws Exception {
        String accessKey = "AKIDEXAMPLE";
        String secretKey = "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY";
        String regionName = "us-east-1";
        String serviceName = "iam";

        cfg = new AwsConfiguration(
                accessKey,
                secretKey,
                regionName,
                serviceName
        );
    }

    @Test
    // See: https://docs.aws.amazon.com/general/latest/gr/signature-v4-examples.html#signature-v4-examples-java
    public void test_AWS_SIG4_Signature() {
        String expected = "c4afb1cc5771d871763a393e44b703571b55cc28424d1a5e86da6ed3c154a4b9";

        ZonedDateTime timestamp = ZonedDateTime.parse("2015-08-30T12:36:00.000Z", DateTimeFormatter.ISO_DATE_TIME);

        String signature = Tools.getSignatureKeyAsString(
                cfg.awsSecretKey,
                timestamp,
                cfg.awsRegion,
                cfg.awsServiceName
        );
        assertThat(signature).isEqualTo(expected);
    }

    // See also: http://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
    @Test
    public void test_AWS_SIG4_request_signature() throws IOException {
        String expectedSignature = "5d672d79c15b13162d9279b0855cfba6789a8edb4c82c400e06b5924a6f2b5d7";

        String stringToSign = "AWS4-HMAC-SHA256\n" +
                "20150830T123600Z\n" +
                "20150830/us-east-1/iam/aws4_request\n" +
                "f536975d06c0309214f805bb90ccff089219ecd68b2577efef23edd43b7e1a59";

        String signature = Tools.createSignature(
                base16().lowerCase().decode("c4afb1cc5771d871763a393e44b703571b55cc28424d1a5e86da6ed3c154a4b9"),
                stringToSign
        );

        assertThat(signature).isEqualTo(expectedSignature);
    }


}