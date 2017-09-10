package com.github.esiqveland.okhttp3;

import com.github.esiqveland.okhttp3.utils.Tools;
import com.github.esiqveland.okhttp3.utils.Utils;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestSuiteTest {

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

    /**
     * file-name.req—the web request to be signed.
     * file-name.creq—the resulting canonical request.
     * file-name.sts—the resulting string to sign.
     * file-name.authz—the Authorization header.
     * file-name.sreq— the signed request.
     */
    private final ImmutableList<String> normalTests = ImmutableList.of(
            "get-vanilla-query-order-key",
            "get-header-key-duplicate",
            "get-header-value-order",
            "get-header-value-trim",
            "get-unreserved",
            "get-utf8",
            "get-vanilla",
            "get-vanilla-query",
            "get-vanilla-query-order-value",
            "get-vanilla-query-order-key-case",
            "get-vanilla-query-unreserved",
            "get-vanilla-empty-query-key",
            "post-header-key-case",
            "post-header-key-sort",
            "post-header-value-case",
            "post-vanilla",
            "post-vanilla-query",
            "post-x-www-form-urlencoded",
            "post-x-www-form-urlencoded-parameters",
            "post-vanilla-empty-query-value"
    );

    // tests around normalization of relative paths
    private final ImmutableList<String> normalizeTests = ImmutableList.of(
            "get-slash",
            "get-slashes",
            "get-space",
            "get-relative",
            "get-slash-dot-slash",
            "get-relative-relative",
            "get-slash-pointless-dot"
    );

    @Test
    @Ignore("Newline in header value is forbidden by OkHttp")
    public void testMultiLineHeader() throws IOException {
        String expectedAuthorization = "AWS4-HMAC-SHA256 Credential=AKIDEXAMPLE/20150830/us-east-1/service/aws4_request, SignedHeaders=host;my-header1;x-amz-date, Signature=ba17b383a53190154eb5fa66a1b836cc297cc0a3d70a5d00705980573d8ff790";

        Request req = new Request.Builder()
                .get()
                .url("http://example.amazonaws.com/")
                .header("Host", "example.amazonaws.com")
                .header("X-Amz-Date", "20150830T123600Z")
                .header("My-Header1", "value1\n" +
                        "  value2\n" +
                        "     value3"
                )
                .build();

        runTestExpectingHeader(req, expectedAuthorization);
    }

    @TestFactory
    public Stream<DynamicTest> testRequestURLNormalization() {
        return normalizeTests.stream()
                .map(dataSet -> DynamicTest.dynamicTest(dataSet, () -> runTest("normalize-path", dataSet)));
    }

    @TestFactory
    public Stream<DynamicTest> testRequests() {
        return normalTests.stream()
                .map(dataSet -> DynamicTest.dynamicTest(dataSet, () -> runTest("./", dataSet)));
    }

    private void runTest(String folder, String dataset) throws IOException {
        String authzFile = String.format("/testdata/aws-sigv4/%s/%s/%s.authz", folder, dataset, dataset);
        String expected = readResource(authzFile);

        String requestFile = String.format("/testdata/aws-sigv4/%s/%s/%s.req", folder, dataset, dataset);
        Request req = parseRequest(readResource(requestFile)).build();

        runTestExpectingHeader(req, expected);
    }

    private void runTestExpectingHeader(Request req, String expectAuthHeader) throws IOException {
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

        assertThat(awsHeader).isEqualTo(expectAuthHeader);
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
        String path = Joiner.on(" ").join(Arrays.copyOfRange(spec, 1, spec.length-1));
        String httpV = spec[spec.length - 1];
        RequestBody body = "GET".equals(method) ? null : RequestBody.create(null, new byte[0]);

        Request.Builder builder = new Request.Builder()
                .method(method, body);

        String host = "";
        String aBody = null;
        for (int i = 1; i < split.length; i++) {
            if (Utils.isBlank(split[i])) {
                String[] bodyStrings = Arrays.copyOfRange(split, i + 1, split.length);
                aBody = Joiner.on("\n").join(bodyStrings);
                break;
            }
            String[] header = split[i].split(":");
            builder.addHeader(header[0], header[1]);

            if ("Host".equals(header[0])) {
                host = header[1];
            }
        }
        if (aBody != null) {
            builder.method(method, RequestBody.create(null, aBody.getBytes(Charsets.UTF_8)));
        }

        String url = String.format("http://%s%s", host, path);
        builder.url(url);

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
