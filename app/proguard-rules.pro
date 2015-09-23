# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/ganghan-yong/Documents/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# OrmLite uses reflection
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }
-keep class com.kakao.** { *; }
-keepattributes Signature
-keepclassmembers class * {
  public static <fields>;
  public *;
}
-dontwarn android.support.v4.**, com.ning.http.client.**, org.jboss.netty.**, org.slf4j.**, com.google.android.gms.**
-dontwarn com.kakao.**
-keep class uk.co.chrisjenx.** { *; }
-keep class com.bridge4biz.laundry.gcm.** { *; }
-keep class com.bridge4biz.laundry.io.** { *; }
-keep public class * extends com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper {
    public <init> (android.content.Context);
}
-keepclassmembers class * {
    public <init> (android.content.Context);
}
-libraryjars libs
-keep class com.urqa.** { *; }
-dontskipnonpubliclibraryclasses
-printmapping out.map
-keep class io.card.**
-keepclassmembers class io.card.** {
    *;
}
# We only want obfuscation
-keepattributes InnerClasses, Signature, Annotation, EnclosingMethod
# Chat sdk
-keep public interface com.zopim.android.sdk.** { *; }
-keep public class com.zopim.android.sdk.** { *; }
# OKHttp
-dontwarn com.squareup.okhttp.**
# Jackson
-keep public interface com.fasterxml.jackson.** { *; }
-keep public class com.fasterxml.jackson.** { *; }
-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry
# Appcompat and support
-keep interface android.support.v7.** { *; }
-keep class android.support.v7.** { *; }
