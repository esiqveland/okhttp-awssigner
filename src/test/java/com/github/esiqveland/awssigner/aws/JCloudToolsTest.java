package com.github.esiqveland.awssigner.aws;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JCloudToolsTest {

    @Test
    public void getEmptyPayloadContentHash() throws Exception {
        String emptyHash = JCloudTools.getEmptyPayloadContentHash();
        assertThat(emptyHash).isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }


}