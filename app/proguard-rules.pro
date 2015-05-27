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
-dontwarn android.support.v4.**,com.ning.http.client.**,org.jboss.netty.**, org.slf4j.**, com.fasterxml.jackson.databind.**, com.google.android.gms.**
-dontwarn com.squareup.okhttp.**
-dontwarn com.kakao.**
-keep class com.bridge4biz.laundry.gcm.** { *; }
-keep class com.bridge4biz.laundry.io.** { *; }
-keep public class * extends com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper {
    public <init> (android.content.Context);
}
-keepclassmembers class * {
    public <init> (android.content.Context);
}