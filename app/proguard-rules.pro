## =============================================================================
## PdfReader – ProGuard / R8 rules (release)
## Obfuscates code, strips unused resources, and keeps required reflection/JNI.
## =============================================================================
#
## -----------------------------------------------------------------------------
## General Android / Kotlin
## -----------------------------------------------------------------------------
#-keepattributes *Annotation*
#-keepattributes Signature
#-keepattributes InnerClasses
#-keepattributes EnclosingMethod
#-keepattributes Exceptions
#-keepattributes SourceFile,LineNumberTable
#-renamesourcefileattribute SourceFile
#
#-keep class kotlin.Metadata { *; }
#-keepclassmembers class **$WhenMappings { <fields>; }
#-keepclassmembers class kotlin.coroutines.** { *; }
#
#-dontwarn kotlin.**
#-dontwarn javax.annotation.**
#-dontwarn org.codehaus.mojo.animal_sniffer.**
#
## -----------------------------------------------------------------------------
## Security – remove debug logging in release builds
## -----------------------------------------------------------------------------
#-assumenosideeffects class android.util.Log {
#    public static boolean isLoggable(java.lang.String, int);
#    public static int v(...);
#    public static int d(...);
#    public static int i(...);
#}
#
## -----------------------------------------------------------------------------
## Application entry points (manifest-declared components)
## -----------------------------------------------------------------------------
#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Application
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.appwidget.AppWidgetProvider
#-keep public class * extends androidx.fragment.app.Fragment
#
#-keep class com.reader.alldocviewer.pdfeditor.ApplicationClass { *; }
#
## -----------------------------------------------------------------------------
## View Binding & Data Binding
## -----------------------------------------------------------------------------
#-keep class * implements androidx.viewbinding.ViewBinding {
#    public static *** bind(android.view.View);
#    public static *** inflate(...);
#}
#
#-keep class androidx.databinding.** { *; }
#-dontwarn androidx.databinding.**
#
## -----------------------------------------------------------------------------
## Parcelable & Serializable
## -----------------------------------------------------------------------------
#-keepclassmembers class * implements android.os.Parcelable {
#    public static final android.os.Parcelable$Creator CREATOR;
#}
#
#-keepclassmembers class * implements java.io.Serializable {
#    static final long serialVersionUID;
#    private static final java.io.ObjectStreamField[] serialPersistentFields;
#    private void writeObject(java.io.ObjectOutputStream);
#    private void readObject(java.io.ObjectInputStream);
#    java.lang.Object writeReplace();
#    java.lang.Object readResolve();
#}
#
#-keep class com.reader.alldocviewer.pdfeditor.CallFunction.** { *; }
#
## Call-end overlay launch helpers (related package)
#-keep class com.reader.alldocviewer.pdfeditor.Advertisement.CallendClass.** { *; }
#-keepnames class com.reader.alldocviewer.pdfeditor.Advertisement.CallendClass.PhoneCallStateService
#
## -----------------------------------------------------------------------------
## Dagger Hilt
## -----------------------------------------------------------------------------
#-keep class dagger.hilt.** { *; }
#-keep class javax.inject.** { *; }
#-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
#
#-keepclasseswithmembers class * {
#    @dagger.* <methods>;
#}
#-keepclasseswithmembers class * {
#    @javax.inject.* <fields>;
#}
#-keepclasseswithmembers class * {
#    @javax.inject.* <methods>;
#}
#
#-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
#-keep @dagger.hilt.InstallIn class * { *; }
#-keep @dagger.Module class * { *; }
#
#-keep class com.reader.alldocviewer.pdfeditor.di.** { *; }
#-keep class **_HiltModules { *; }
#-keep class **_HiltModules$* { *; }
#-keep class **_GeneratedInjector { *; }
#-keep class **_MembersInjector { *; }
#-keep class **_Factory { *; }
#-keep class **_Provide*Factory { *; }
#
#-dontwarn com.google.errorprone.annotations.**
#
## -----------------------------------------------------------------------------
## Room Database
## -----------------------------------------------------------------------------
#-keep class * extends androidx.room.RoomDatabase { *; }
#-keep @androidx.room.Entity class * { *; }
#-keep @androidx.room.Dao interface * { *; }
#-keepclassmembers class * {
#    @androidx.room.* <methods>;
#}
#
#-keep class com.reader.alldocviewer.pdfeditor.data.local.** { *; }
#
## -----------------------------------------------------------------------------
## Gson
## -----------------------------------------------------------------------------
#-keepattributes Signature
#-keep class com.google.gson.** { *; }
#-keep class * extends com.google.gson.TypeToken
#-keep class * extends com.google.gson.reflect.TypeToken
#
#-keepclassmembers,allowobfuscation class * {
#    @com.google.gson.annotations.SerializedName <fields>;
#}
#
## -----------------------------------------------------------------------------
## Glide
## -----------------------------------------------------------------------------
#-keep public class * implements com.bumptech.glide.module.GlideModule
#-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
#-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
#    **[] $VALUES;
#    public *;
#}
#-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder { *** rewind(); }
#
#-dontwarn com.bumptech.glide.**
#
## -----------------------------------------------------------------------------
## Lottie
## -----------------------------------------------------------------------------
#-keep class com.airbnb.lottie.** { *; }
#-dontwarn com.airbnb.lottie.**
#
## -----------------------------------------------------------------------------
## EventBus
## -----------------------------------------------------------------------------
#-keepattributes *Annotation*
#-keepclassmembers class * {
#    @org.greenrobot.eventbus.Subscribe <methods>;
#}
#-keep enum org.greenrobot.eventbus.ThreadMode { *; }
#-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
#    <init>(java.lang.Throwable);
#}
#
## -----------------------------------------------------------------------------
### Firebase (Crashlytics, Analytics, Config, Messaging)
### -----------------------------------------------------------------------------
##-keep class com.google.firebase.** { *; }
##-keep class com.google.android.gms.** { *; }
##-dontwarn com.google.firebase.**
##-dontwarn com.google.android.gms.**
#
#-keepattributes SourceFile,LineNumberTable
#-keep public class * extends java.lang.Exception
#
## -----------------------------------------------------------------------------
## Google Play In-App Update
## -----------------------------------------------------------------------------
#-keep class com.google.android.play.core.** { *; }
#-dontwarn com.google.android.play.core.**
#
## -----------------------------------------------------------------------------
## ML Kit Document Scanner
## -----------------------------------------------------------------------------
#-keep class com.google.mlkit.** { *; }
#-dontwarn com.google.mlkit.**
#
## -----------------------------------------------------------------------------
## SmartCropper
## -----------------------------------------------------------------------------
#-keep class com.moizhassan.smartcropper.** { *; }
#-dontwarn com.moizhassan.smartcropper.**
#
## -----------------------------------------------------------------------------
## PDF – iText
## -----------------------------------------------------------------------------
#-keep class com.itextpdf.** { *; }
#-dontwarn com.itextpdf.**
#
## -----------------------------------------------------------------------------
## PDF – PDFBox Android
## -----------------------------------------------------------------------------
#-keep class com.tom_roush.pdfbox.** { *; }
#-keep class org.apache.pdfbox.** { *; }
#-dontwarn com.tom_roush.pdfbox.**
#-dontwarn org.apache.pdfbox.**
#
## -----------------------------------------------------------------------------
## PDF – AndroidPdfViewer / Pdfium
## -----------------------------------------------------------------------------
#-keep class com.github.barteksc.pdfviewer.** { *; }
#-keep class com.shockwave.pdfium.** { *; }
#-keep class com.reader.alldocviewer.pdfeditor.pdfviewer.** { *; }
#-dontwarn com.github.barteksc.pdfviewer.**
#-dontwarn com.shockwave.pdfium.**
#
## -----------------------------------------------------------------------------
## MuPDF – JNI native bindings (do not obfuscate native method names)
## -----------------------------------------------------------------------------
#-keepclasseswithmembernames,includedescriptorclasses class * {
#    native <methods>;
#}
#
#-keep @androidx.annotation.Keep class * { *; }
#-keepclassmembers class * {
#    @androidx.annotation.Keep *;
#}
#
#-keep class com.artifex.mupdf.** { *; }
#-keep class com.artifex.mupdfdemo.** { *; }
#
## -----------------------------------------------------------------------------
## Spongycastle / BouncyCastle (PDF encryption)
## -----------------------------------------------------------------------------
#-keep class org.spongycastle.** { *; }
#-keep class com.madgag.spongycastle.** { *; }
#-dontwarn org.spongycastle.**
#-dontwarn com.madgag.spongycastle.**
#
## -----------------------------------------------------------------------------
## Jersey (encode/decode)
## -----------------------------------------------------------------------------
#-keep class com.sun.jersey.** { *; }
#-dontwarn com.sun.jersey.**
#-dontwarn javax.ws.**
#-dontwarn javax.xml.**
#
## -----------------------------------------------------------------------------
## Office document viewer (dom4j reflection, Apache POI internals)
## -----------------------------------------------------------------------------
#-keep class com.reader.alldocviewer.pdfeditor.** { *; }
#-keep class org.dom4j.** { *; }
#-dontwarn com.reader.alldocviewer.pdfeditor.**
#-dontwarn org.dom4j.**
#
## -----------------------------------------------------------------------------
## RoundedImageView, Shimmer, SDP
## -----------------------------------------------------------------------------
#-keep class com.makeramen.roundedimageview.** { *; }
#-keep class com.facebook.shimmer.** { *; }
#-dontwarn com.intuit.sdp.**
#
## -----------------------------------------------------------------------------
## Advertisement / remote-config helpers
## -----------------------------------------------------------------------------
#-keep class com.reader.alldocviewer.pdfeditor.Advertisement.** { *; }
#
## -----------------------------------------------------------------------------
## BuildConfig (generated fields referenced in code)
## -----------------------------------------------------------------------------
#-keep class com.reader.alldocviewer.pdfeditor.BuildConfig { *; }
#
## -----------------------------------------------------------------------------
## Enum classes
## -----------------------------------------------------------------------------
#-keepclassmembers enum * {
#    public static **[] values();
#    public static ** valueOf(java.lang.String);
#}
#
## -----------------------------------------------------------------------------
## Android R classes (required when code references resources by ID)
## -----------------------------------------------------------------------------
#-keepclassmembers class **.R$* {
#    public static <fields>;
#}
#
## -----------------------------------------------------------------------------
## Suppress common third-party warnings
## -----------------------------------------------------------------------------
#-dontwarn java.awt.**
#-dontwarn javax.swing.**
#-dontwarn org.apache.commons.**
#-dontwarn org.bouncycastle.**
#-dontwarn org.conscrypt.**
#-dontwarn org.ietf.jgss.**
#-dontwarn org.w3c.dom.**
#-dontwarn org.xml.sax.**
#
## Please add these rules to your existing keep rules in order to suppress warnings.
## This is generated automatically by the Android Gradle plugin.
#-dontwarn com.facebook.infer.annotation.Nullsafe$Mode
#-dontwarn com.facebook.infer.annotation.Nullsafe





# EventBus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }


-renamesourcefileattribute SourceFile
-keepattributes SourceFile, LineNumberTable

-dontwarn com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
-dontwarn com.bumptech.glide.load.resource.bitmap.Downsampler
-dontwarn com.bumptech.glide.load.resource.bitmap.HardwareConfigState
-dontwarn com.bumptech.glide.manager.RequestManagerRetriever

-keep public class * extends java.lang.Exception

# Joda
-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn javax.swing.tree.TreeNode

#Gson https://github.com/google/gson/blob/main/gson/src/main/resources/META-INF/proguard/gson.pro
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowoptimization @com.google.gson.annotations.JsonAdapter class *

-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.Expose <fields>;
  @com.google.gson.annotations.JsonAdapter <fields>;
  @com.google.gson.annotations.Since <fields>;
  @com.google.gson.annotations.Until <fields>;
}

-keepclassmembers class * extends com.google.gson.TypeAdapter {
  <init>();
}
-keepclassmembers class * implements com.google.gson.TypeAdapterFactory {
  <init>();
}
-keepclassmembers class * implements com.google.gson.JsonSerializer {
  <init>();
}
-keepclassmembers class * implements com.google.gson.JsonDeserializer {
  <init>();
}

-if class *
-keepclasseswithmembers,allowobfuscation class <1> {
  @com.google.gson.annotations.SerializedName <fields>;
}
-if class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers,allowobfuscation,allowoptimization class <1> {
  <init>();
}


# Keep crypto classes
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }

# Keep Base64
-keep class android.util.Base64 { *; }

# If you store keys in strings.xml
-keepclassmembers class * {
    public static <fields>;
}
-keep class com.reader.alldocviewer.pdfeditor.data.local.** { *; }
-keep class com.reader.alldocviewer.pdfeditor.managepdf.PdfPage { *; }

# Keep all model classes recursively
-keep class **.model.** { *; }
-keep class **.Model.** { *; }

# Keep CallFunction and Advertisement packages (required for CallEndActivity and ADSMainClass)
-keep class com.reader.alldocviewer.pdfeditor.CallFunction.** { *; }
-keep class com.reader.alldocviewer.pdfeditor.Advertisement.** { *; }

# Keep Guava TypeToken (just in case)
-keep class com.google.common.reflect.TypeToken { *; }
-keep class * extends com.google.common.reflect.TypeToken

# MuPDF JNI native bindings (repackaged)
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}
-keep class com.artifex.** { *; }

# -----------------------------------------------------------------------------
# Office document viewer (embedded POI/dom4j — heavy reflection; stripped by R8 in release)
# -----------------------------------------------------------------------------
-keep class com.reader.alldocviewer.pdfeditor.** { *; }
-keepclassmembers class * extends com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.fc.hslf.record.Record {
    <init>(byte[], int, int);
}
-keepclassmembers class com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.res.ResConstant {
    public static <fields>;
}
-keep class com.reader.alldocviewer.pdfeditor.ui.activities.officeviewer.** { *; }
-keep class com.reader.alldocviewer.pdfeditor.utils.OfficeFileOpenHelper { *; }
-keep class com.reader.alldocviewer.pdfeditor.utils.OfficeViewerLauncher { *; }
-dontwarn com.reader.alldocviewer.pdfeditor.**
-dontwarn org.dom4j.**
-dontwarn org.apache.poi.**
-dontwarn javax.xml.stream.**

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn com.facebook.infer.annotation.Nullsafe$Mode
-dontwarn com.facebook.infer.annotation.Nullsafe
-dontwarn javax.activation.DataSource
-dontwarn javax.mail.internet.MimeMultipart


# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn org.spongycastle.cert.X509CertificateHolder
-dontwarn org.spongycastle.cms.CMSEnvelopedData
-dontwarn org.spongycastle.cms.Recipient
-dontwarn org.spongycastle.cms.RecipientId
-dontwarn org.spongycastle.cms.RecipientInformation
-dontwarn org.spongycastle.cms.RecipientInformationStore
-dontwarn org.spongycastle.cms.jcajce.JceKeyTransEnvelopedRecipient
-dontwarn org.spongycastle.cms.jcajce.JceKeyTransRecipient
