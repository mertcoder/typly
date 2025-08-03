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

# ============ AGORA SDK PROGUARD RULES ============
# Keep all Agora SDK classes
-keep class io.agora.**{*;}
-dontwarn io.agora.**

# Keep native methods 
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Agora RTC Engine
-keep class io.agora.rtc.** { *; }
-dontwarn io.agora.rtc.**

# Keep Agora base classes
-keep class io.agora.base.** { *; }
-dontwarn io.agora.base.**

# Keep reflection-based classes
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}