package com.github.esiqveland.awssigner;

import com.github.esiqveland.awssigner.aws.Tools;
import com.google.common.collect.ImmutableList;
import io.vavr.control.Try;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class RequestTestSuite {

    private AwsConfiguration cfg;
    private ZonedDateTime aDate = ZonedDateTime.parse("2015-08-30T12:36:00.000Z", DateTimeFormatter.ISO_DATE_TIME);
    private AwsSigningInterceptor interceptor;
    private byte[] signatureKey;

    @Before
    public void setup() {
        String accessKey = "AKIDEXAMPLE";
        String secretKey = "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY";
        String regionName = "us-east-1";
        String serviceName = "service";

        cfg = new AwsConfiguration(
                accessKey,
                secretKey,
                regionName,
                serviceName
        );

        Supplier<ZonedDateTime> clock = () -> aDate;
        interceptor = new AwsSigningInterceptor(
                cfg,
                clock
        );

        signatureKey = Tools.getSignatureKey(
                cfg.awsSecretKey,
                aDate,
                cfg.awsRegion,
                cfg.awsServiceName
        );

    }

    private final ImmutableList<String> testData = ImmutableList.of(
            "post-vanilla",
            "get-vanilla",
            "get-vanilla-query",
            "get-vanilla-query-order-key-case",
            "get-vanilla-query-unreserved",
            "get-unreserved",
            "get-utf8",
            "post-vanilla-query"
    );

    @TestFactory
    public Stream<DynamicTest> testAllSets() {
        return testData.stream()
                .map(dataSet -> DynamicTest.dynamicTest(dataSet, () -> runTest(dataSet)));
    }

    private void runTest(String dataset) throws IOException {
        String authzFile = String.format("/testdata/aws-sigv4/%s/%s.authz", dataset, dataset);
        String expected = readResource(authzFile);

        String requestFile = String.format("/testdata/aws-sigv4/%s/%s.req", dataset, dataset);
        Request req = parseRequest(readResource(requestFile)).build();


        String accessKey = "AKIDEXAMPLE";
        String secretKey = "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY";
        String regionName = "us-east-1";
        String serviceName = "service";

        AwsConfiguration cfg = new AwsConfiguration(
                accessKey,
                secretKey,
                regionName,
                serviceName
        );

        Supplier<ZonedDateTime> clock = () -> aDate;
        AwsSigningInterceptor interceptor = new AwsSigningInterceptor(
                cfg,
                clock
        );

        byte[] signatureKey = Tools.getSignatureKey(
                cfg.awsSecretKey,
                aDate,
                cfg.awsRegion,
                cfg.awsServiceName
        );


        String awsHeader = interceptor.makeAWSAuthorizationHeader(aDate, req, signatureKey);

        assertThat(awsHeader).isEqualTo(expected);
    }

    @Test
    public void testRequestParser() throws IOException {
        String expected = readResource("/testdata/aws-sigv4/post-vanilla/post-vanilla.authz");

        Request req = parseRequest(readResource("/testdata/aws-sigv4/post-vanilla/post-vanilla.req")).build();

        String awsHeader = interceptor.makeAWSAuthorizationHeader(aDate, req, signatureKey);

        assertThat(awsHeader).isEqualTo(expected);
    }

    private static Request.Builder parseRequest(String reqFile) {
        String[] split = reqFile.split("\n");

        // TODO: this breaks if there is a whitespace in url
        // Example: "POST /new HTTP/1.1"
        String[] spec = split[0].split(" ");
        String method = spec[0];
        String path = spec[1];
        String httpV = spec[2];
        RequestBody body = "GET".equals(method) ? null : RequestBody.create(null, new byte[0]);

        Request.Builder builder = new Request.Builder()
                .method(method, body);

        String host = "";
        for (int i = 1; i < split.length; i++) {
            String[] header = split[i].split(":");
            builder.addHeader(header[0], header[1]);

            if ("Host".equals(header[0])) {
                host = header[1];
            }
        }

        String url = String.format("http://%s%s", host, path);
        builder.url(url);

        // TODO: how to parse a body?
        return builder;
    }

    private static String readResource(String filename) {
        InputStream stream = AwsSigningInterceptorTest.class.getResourceAsStream(filename);
        try {
            return new Buffer().readFrom(stream).readUtf8();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
