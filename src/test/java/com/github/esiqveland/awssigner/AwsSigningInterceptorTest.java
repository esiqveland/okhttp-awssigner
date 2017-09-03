package com.github.esiqveland.awssigner;

import com.github.esiqveland.awssigner.aws.Tools;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// TODO: add tests for the inclusion of x-amz-date header and its calculation
public class AwsSigningInterceptorTest {
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

    // See also: http://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
    @Test
    public void test_AWS_SIG4_string_to_sign() throws IOException {
        String expected = "AWS4-HMAC-SHA256\n" +
                "20150830T123600Z\n" +
                "20150830/us-east-1/iam/aws4_request\n" +
                "f536975d06c0309214f805bb90ccff089219ecd68b2577efef23edd43b7e1a59";

        ZonedDateTime aDate = ZonedDateTime.parse("2015-08-30T12:36:00.000Z", DateTimeFormatter.ISO_DATE_TIME);
        Supplier<ZonedDateTime> clock = () -> aDate;
        AwsSigningInterceptor interceptor = new AwsSigningInterceptor(cfg, clock);


        String requestHash = "f536975d06c0309214f805bb90ccff089219ecd68b2577efef23edd43b7e1a59";
        String stringToSign = interceptor.createStringToSign(aDate, requestHash);

        assertThat(stringToSign).isEqualTo(expected);
    }

    @Test
    // TODO: finish this test
    public void testWithClient() {
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
                .addNetworkInterceptor(awsInterceptor)
                .build();

    }

    // See also: http://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
    @Test
    public void test_AWS_SIG4_authorization_header_generation() throws IOException {
        String expectedHeader =
                "AWS4-HMAC-SHA256 Credential=AKIDEXAMPLE/20150830/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-amz-date, Signature=5d672d79c15b13162d9279b0855cfba6789a8edb4c82c400e06b5924a6f2b5d7";

        Request req = createExampleRequest()
                .build();

        ZonedDateTime aDate = ZonedDateTime.parse("2015-08-30T12:36:00.000Z", DateTimeFormatter.ISO_DATE_TIME);
        Supplier<ZonedDateTime> clock = () -> aDate;
        AwsSigningInterceptor interceptor = new AwsSigningInterceptor(cfg, clock);

        byte[] signatureKey = Tools.getSignatureKey(cfg.awsSecretKey, aDate, cfg.awsRegion, cfg.awsServiceName);
        String requestHash = interceptor.makeAWSAuthorizationHeader(aDate, req, signatureKey);

        assertThat(requestHash).isEqualTo(expectedHeader);
    }

    // See also: http://docs.aws.amazon.com/general/latest/gr/signature-v4-test-suite.html
    @Test
    public void test_AWS_SIG4_authorization_header_GET() throws IOException {
        String expectedHeader =
                "AWS4-HMAC-SHA256 Credential=AKIDEXAMPLE/20150830/us-east-1/service/aws4_request, SignedHeaders=host;x-amz-date, Signature=b97d918cfa904a5beff61c982a1b6f458b799221646efd99d3219ec94cdf2500";

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

        Request req = new Request.Builder()
                .get()
                .addHeader("Host", "example.amazonaws.com")
                .url("https://example.amazonaws.com/?Param2=value2&Param1=value1")
                .build();

        ZonedDateTime aDate = ZonedDateTime.parse("2015-08-30T12:36:00.000Z", DateTimeFormatter.ISO_DATE_TIME);
        Supplier<ZonedDateTime> clock = () -> aDate;
        AwsSigningInterceptor interceptor = new AwsSigningInterceptor(cfg, clock);

        byte[] signatureKey = Tools.getSignatureKey(cfg.awsSecretKey, aDate, cfg.awsRegion, cfg.awsServiceName);
        String requestHash = interceptor.makeAWSAuthorizationHeader(aDate, req, signatureKey);

        assertThat(requestHash).isEqualTo(expectedHeader);
    }


    @Test
    // See: https://docs.aws.amazon.com/general/latest/gr/sigv4-calculate-signature.html
    public void test_AWS_SIG4_Signature_key() {
        String expected = "c4afb1cc5771d871763a393e44b703571b55cc28424d1a5e86da6ed3c154a4b9";

        ZonedDateTime timestamp = ZonedDateTime.parse("2015-08-30T12:36:00.000Z", DateTimeFormatter.ISO_DATE_TIME);

        String signature = Tools.getSignatureKeyAsString(
                "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY",
                timestamp,
                "us-east-1",
                "iam"
        );
        assertThat(signature).isEqualTo(expected);
    }

    // See also: http://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
    @Test
    public void test_AWS_SIG4_build_signing_string() throws IOException {
        String expected = "GET\n" +
                "/\n" +
                "Action=ListUsers&Version=2010-05-08\n" +
                "content-type:application/x-www-form-urlencoded; charset=utf-8\n" +
                "host:iam.amazonaws.com\n" +
                "x-amz-date:20150830T123600Z\n" +
                "\n" +
                "content-type;host;x-amz-date\n" +
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        Request req = createExampleRequest()
                .build();

        ZonedDateTime aDate = ZonedDateTime.parse("2015-08-30T12:36:00.000Z", DateTimeFormatter.ISO_DATE_TIME);
        Supplier<ZonedDateTime> clock = () -> aDate;
        AwsSigningInterceptor interceptor = new AwsSigningInterceptor(cfg, clock);

        String canonicalRequest = interceptor.createCanonicalRequest(aDate, req);

        assertThat(canonicalRequest).isEqualTo(expected);
    }


    // See also: http://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
    @Test
    public void test_AWS_SIG4_final_header_signature() throws IOException {
        String expectedHeader = "AWS4-HMAC-SHA256 Credential=AKIDEXAMPLE/20150830/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-amz-date, Signature=5d672d79c15b13162d9279b0855cfba6789a8edb4c82c400e06b5924a6f2b5d7";

        Request req = createExampleRequest()
                .build();

        ZonedDateTime aDate = ZonedDateTime.parse("2015-08-30T12:36:00.000Z", DateTimeFormatter.ISO_DATE_TIME);
        Supplier<ZonedDateTime> clock = () -> aDate;
        AwsSigningInterceptor interceptor = new AwsSigningInterceptor(cfg, clock);

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        when(chain.request()).thenReturn(req);

        // invoke interceptor chain
        interceptor.intercept(chain);

        verify(chain).proceed(captor.capture());

        Request finalRequest = captor.getValue();
        assertThat(finalRequest).isNotNull();
        assertThat(finalRequest.header("Authorization")).isEqualTo(expectedHeader);
    }


    // See also: http://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
    @Test
    public void test_AWS_SIG4_request_is_chained() throws IOException {
        ZonedDateTime aDate = ZonedDateTime.parse("2015-08-30T12:36:00.000Z", DateTimeFormatter.ISO_DATE_TIME);
        Supplier<ZonedDateTime> clock = () -> aDate;
        AwsSigningInterceptor interceptor = new AwsSigningInterceptor(cfg, clock);

        Request req = createExampleRequest()
                .build();

        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        when(chain.request()).thenReturn(req);

        interceptor.intercept(chain);

        verify(chain, times(1)).proceed(any());
    }

    static Request.Builder createExampleRequest() {
        return new Request.Builder()
                .get()
                .url("https://iam.amazonaws.com/?Action=ListUsers&Version=2010-05-08")
                .addHeader("Host", "iam.amazonaws.com")
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                .addHeader("X-Amz-Date", "20150830T123600Z");
    }

}