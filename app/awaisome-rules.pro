-keep class **.R { *; }
-keep class **.R$* { public static final int *; }
-keep class **$Properties

-keep,allowoptimization public class androidx.webkit.**
-keep,allowoptimization public class * extends androidx.webkit.**
-keep,allowoptimization public class * extends androidx.multidex.**

-keepclassmembernames,allowoptimization class * implements java.lang.annotation.Annotation
-keepclassmembernames,allowoptimization public class * extends android.database.ContentObserver
-keepnames public class android.webkit.*
-keep public class * extends android.app.Service
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.content.BroadcastReceiver
-keep public class * implements android.os.IInterface
-keep,allowobfuscation,allowoptimization interface <1>

-keep public class * implements android.os.Parcelable { public static final *; }
-keepclassmembernames public class * extends android.view.View {
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembernames,allowoptimization public class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keepclassmembers,allowoptimization enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


#### start Ad Adapters ####
-keep class com.adcolony.** { *; }
-keep class com.truenet.** { *; }
-keep class com.startapp.** { *; }
-keep class com.applovin.** { *; }
-keep class com.facebook.ads.** { *; }
-keep class com.appodeal.** { *; }
-keep class org.nexage.** { *; }

-dontwarn com.adcolony.**
-dontwarn com.startapp.**
-dontwarn com.applovin.**
-dontwarn com.appodeal.**
-dontwarn org.nexage.**
-dontwarn com.facebook.ads.**
#### end Ad Adapters ####

################## GOOGLE ##################
-keep class com.google.ads.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.google.android.** { *; }
-keep class com.google.firebase.** { *; }
-keep class com.google.googlesignin.** { *; }
-keep class com.google.android.gms.** { *; }
-keep public class com.google.android.gms.** { public protected *; }
-keep class com.google.common.** { *; }
-keep class com.crashlytics.** { *; }

-dontwarn com.google.ads.**
-dontwarn com.google.common.**
-dontwarn com.google.android.gms.**

################## OTHERS ##################
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keep class org.apache.** { *; }
-keep class com.shaded.fasterxml.jackson.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-if interface * { @retrofit2.http.* <methods>; }
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }
-keepclassmembernames interface * { @retrofit2.http.* <methods>; }

-dontwarn com.crashlytics.**
-dontwarn rx.*
-dontwarn okio.**
-dontwarn org.apache.**
-dontwarn com.squareup.okhttp3.**
-dontwarn retrofit.**
-dontwarn retrofit2.**
-dontwarn java.lang.invoke.*
-dontwarn org.jetbrains.annotations.**
-dontwarn android.webkit.JavascriptInterface
-dontwarn android.webkit.WebView
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontwarn org.shaded.apache.**
-dontwarn org.ietf.jgss.**
