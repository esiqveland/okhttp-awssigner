module com.github.esiqveland.okhttp3.awssigner
{
    // wait for versions with Automatic-Module-Name set in MANIFEST.MF
    // See: http://blog.joda.org/2017/05/java-se-9-jpms-automatic-modules.html
    requires okhttp3;
    requires okio;

    requires com.google.common;

    exports com.github.esiqveland.okhttp3.awssigner;
}
