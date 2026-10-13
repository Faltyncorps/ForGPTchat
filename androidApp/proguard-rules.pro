# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses, Signature
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class cz.faltyn.elfall.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class cz.faltyn.elfall.**$$serializer { *; }
# Ktor / OkHttp
-dontwarn org.slf4j.**
-dontwarn io.ktor.**
-dontwarn okhttp3.internal.platform.**
# secp256k1 JNI
-keep class fr.acinq.secp256k1.** { *; }
# zxing
-keep class com.journeyapps.barcodescanner.** { *; }
