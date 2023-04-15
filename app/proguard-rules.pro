#-printmapping proguardMapping.txt

-optimizationpasses 5
-optimizations !code/simplification/cast,!field/*,!class/merging/*
-keepattributes *Annotation*,Exceptions
-keepattributes InnerClasses,LineNumberTable
-keepattributes Signature,SourceFile

-keep class **.R$* { public static final int *; }
-keep class **$Properties

#-keepclassmembers,allowshrinking class awais.backworddictionary.adapters.holders.WordItem { private *; }

-keep,allowshrinking public class android.webkit.*
#-keep,allowshrinking public class * extends androidx.multidex.MultiDexApplication

-keep,allowshrinking class * implements java.lang.annotation.Annotation

-keep public class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keepclassmembernames public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembernames public class * extends android.webkit.WebView {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public *;
    private *;
    *;
}

-keepclassmembers,allowshrinking class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String);
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-dontwarn org.apache.**
-dontwarn java.lang.invoke.*
-dontwarn android.webkit.WebView
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.OpenSSLProvider
