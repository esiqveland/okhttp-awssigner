/**
 * Copyright 2017 Eivind Larsen.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.esiqveland.okhttp3.utils;

import com.google.common.base.Charsets;
import com.google.common.escape.Escaper;
import com.google.common.net.PercentEscaper;
import io.vavr.Tuple;
import okhttp3.HttpUrl;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.io.BaseEncoding.base16;
import static java.util.stream.Collectors.joining;

public class Tools {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd")
            .withZone(ZoneId.of("GMT"));

    // mac is a holder to make sure the algorithm is present at JVM startup
    // and doesn't fail runtime when called
    private final Mac mac = getAlgorithmSilent(HMAC_SHA256, "anykey".getBytes(Charsets.UTF_8));


    static Mac getAlgorithmSilent(String algorithm, byte[] key) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key, algorithm));
            return mac;
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    final static private String HMAC_SHA256 = "HmacSHA256";

    static byte[] HmacSHA256(byte[] key, String data) {
        Mac mac = getAlgorithmSilent(HMAC_SHA256, key);
        return mac.doFinal(data.getBytes(Charsets.UTF_8));
    }

    // See: https://docs.aws.amazon.com/general/latest/gr/signature-v4-examples.html#signature-v4-examples-java
    public static String getSignatureKeyAsString(
            String secretKey,
            ZonedDateTime timestamp,
            String regionName,
            String serviceName
    ) {
        return base16().lowerCase().encode(getSignatureKey(secretKey, timestamp, regionName, serviceName));
    }


    public static String createSignature(byte[] signingKey, String stringToSign) {
        return base16().lowerCase().encode(HmacSHA256(signingKey, stringToSign));
    }

    public static byte[] getSignatureKey(String secretKey, ZonedDateTime dateStamp, String regionName, String serviceName) {
        String theDate = DATE_FORMAT.format(dateStamp);

        byte[] kSecret = ("AWS4" + secretKey).getBytes(Charsets.UTF_8);
        byte[] kDate = HmacSHA256(kSecret, theDate);
        byte[] kRegion = HmacSHA256(kDate, regionName);
        byte[] kService = HmacSHA256(kRegion, serviceName);
        byte[] kSigning = HmacSHA256(kService, "aws4_request");
        return kSigning;
    }

    /**
     * Do not URI-encode any of the unreserved characters that RFC 3986 defines:
     * A-Z, a-z, 0-9, hyphen ( - ), underscore ( _ ), period ( . ), and tilde ( ~ ).
     * <p>
     * Percent-encode all other characters with %XY, where X and Y are hexadecimal characters (0-9 and uppercase A-F).
     * For example, the space character must be encoded as %20 (not using '+', as some encoding schemes do) and extended UTF-8 characters must be in the form %XY%ZA%BC.
     * <p>
     * See: https://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
     */
    private static final Escaper URL_ENCODER = new PercentEscaper("-_.~", false);

    public static String createCanonicalQueryString(HttpUrl url) {
        int querySize = url.querySize();
        if (querySize == 0) {
            return "";
        }
        return url.queryParameterNames().stream()
                .flatMap(key -> url.queryParameterValues(key).stream()
                        .map(value -> Tuple.of(
                                URL_ENCODER.escape(key),
                                URL_ENCODER.escape(value)
                        )))
                .sorted(Comparator.naturalOrder())
                .map(keyVal -> keyVal.apply(
                        (key, val) -> String.format("%s=%s", key, val))
                )
                .collect(joining("&"));
    }

    public static String createCanonicalHeaderString(Map<String, List<String>> headersMap) {
        return headersMap.entrySet().stream()
                .map(entry -> Tuple.of(
                        Utils.lowerCase(entry.getKey()),
                        entry.getValue()
                ))
                .map(tuple -> tuple.map2(valueList -> valueList.stream()
                        .map(Utils::trim)
                        .map(Utils::removeContiguousBlanks)
                        .collect(joining(","))
                ))
                .map(tuple -> tuple.apply(
                        (header, values) -> String.format("%s:%s", header, values)
                ))
                .collect(joining("\n"));
    }

    private static Pattern REDUNDANT_PATHS = Pattern.compile("\\/+");

    public static String getCanonicalPath(HttpUrl url) {
        return REDUNDANT_PATHS.matcher(Utils.defaultIfBlank(
                url.encodedPath(),
                "/"
        )).replaceAll("/");
    }

}
