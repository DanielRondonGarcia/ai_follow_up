# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Room entities and DAOs so schema reflection and generated code are not stripped
-keep class com.example.data.** { *; }

# Keep network model classes so Moshi adapters can reflect on them at runtime
-keep class com.example.network.** { *; }

# Keep methods annotated with @JavascriptInterface so the WebView JS bridge works
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep Moshi @JsonClass annotated classes (codegen path)
-keep @com.squareup.moshi.JsonClass class * { *; }

# OkHttp optional TLS platform dependencies — not present on Android, suppress R8 warnings
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
