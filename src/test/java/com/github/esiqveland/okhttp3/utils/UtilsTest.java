package com.github.esiqveland.okhttp3.utils;

import org.junit.jupiter.api.Test;

import static com.github.esiqveland.okhttp3.utils.Utils.not;
import static org.assertj.core.api.Assertions.assertThat;

class UtilsTest {
    @Test
    void testNot() {
        assertThat(not(false)).isTrue();
        assertThat(not(true)).isFalse();
    }

    @Test
    void testIsBlank() {
        assertThat(Utils.isBlank(null)).isTrue().withFailMessage("Should have been blank");
        assertThat(Utils.isBlank("")).isTrue().withFailMessage("Should have been blank");
        assertThat(Utils.isBlank("   ")).isTrue().withFailMessage("Should have been blank");
        assertThat(Utils.isBlank("   \t ")).isTrue().withFailMessage("Should have been blank");
        assertThat(Utils.isBlank("   \n ")).isTrue().withFailMessage("Should have been blank");
        assertThat(Utils.isBlank("   a  ")).isFalse().withFailMessage("Should not have been blank");
    }

    @Test
    void defaultIfBlank() {
        assertThat(Utils.defaultIfBlank(null, "default")).isEqualTo("default");
        assertThat(Utils.defaultIfBlank("", "default")).isEqualTo("default");
        assertThat(Utils.defaultIfBlank("   ", "default")).isEqualTo("default");
        assertThat(Utils.defaultIfBlank("  \t ", "default")).isEqualTo("default");
        assertThat(Utils.defaultIfBlank("  \n ", "default")).isEqualTo("default");
        assertThat(Utils.defaultIfBlank("a", "default")).isEqualTo("a");
        assertThat(Utils.defaultIfBlank("asdfsdfaasdf", "default")).isEqualTo("asdfsdfaasdf");
    }

    @Test
    void removeContiguousBlanks() {
        assertThat(Utils.removeContiguousBlanks(null)).isEqualTo(null);
        assertThat(Utils.removeContiguousBlanks(" ")).isEqualTo(" ");
        assertThat(Utils.removeContiguousBlanks(" a    b c  ")).isEqualTo(" a b c ");
    }

    @Test
    void trim() {
        assertThat(Utils.trim(null)).isEqualTo(null);
        assertThat(Utils.trim("")).isEqualTo("");
        assertThat(Utils.trim(" ")).isEqualTo("");
        assertThat(Utils.trim("     ")).isEqualTo("");
        assertThat(Utils.trim(" a    b   c  ")).isEqualTo("a    b   c");
    }

    @Test
    void lowerCase() {
        assertThat(Utils.lowerCase(null)).isEqualTo(null);
        assertThat(Utils.lowerCase("ABC")).isEqualTo("abc");
        assertThat(Utils.lowerCase("Host")).isEqualTo("host");
        assertThat(Utils.lowerCase("HostParam")).isEqualTo("hostparam");
    }

}