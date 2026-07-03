# simJ R8 Configuration (Enhanced)
# ================================================

# ========== R8 Full Mode ==========
-allowaccessmodification
-optimizationpasses 5
-repackageclasses ''

# ========== JNI ==========
-keepclasseswithmembernames class * {
    native <methods>;
}

# ========== Telephony API (Reflection) ==========
-keep class android.telephony.** { *; }
-keep class com.sansim.app.esim.TelephonyApduInterface { *; }
-keep class com.sansim.app.esim.TelephonyApduInterface$Companion { *; }

# ========== eSIM Core ==========
-keep class com.sansim.app.esim.** { *; }

# ========== Data Models ==========
-keep class com.sansim.app.data.model.** { *; }

# ========== Kotlin ==========
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepclassmembers class kotlin.reflect.jvm.internal.impl.** {
    void *(***);
}

# ========== Compose ==========
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# ========== Kotlin Coroutines ==========
-keepnames class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ========== CameraX / ML Kit ==========
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# ========== Coil (Image Loading) ==========
-keep class coil.** { *; }
-dontwarn coil.**
-keep class coil.compose.** { *; }

# ========== OkHttp ==========
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ========== JSON ==========
-keep class org.json.** { *; }
-keepclassmembers class * {
    public static org.json.JSONObject toJSONObject(...);
}

# ========== Enum ==========
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ========== Parcelable ==========
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ========== R (Resources) ==========
-keepclassmembers class **.R$* {
    public static <fields>;
}

# ========== Remove Debug Logging ==========
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
}

# ========== Aggressive Optimization ==========
# Remove unused code paths
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}
