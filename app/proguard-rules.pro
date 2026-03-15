# Add project specific ProGuard rules here.
-dontwarn kotlinx.coroutines.**
-keep class com.lottttto.miner.repositories.** { *; }
-keep class com.lottttto.miner.models.** { *; }
-keep class com.lottttto.miner.utils.** { *; }
-keepclasseswithmembernames class * { native <methods>; }
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * implements android.os.Parcelable { public static final android.os.Parcelable$Creator *; }
