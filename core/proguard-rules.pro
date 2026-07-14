# Keep CoreManager class
-keep class com.sansim.core.CoreManager { *; }

# Obfuscate everything else
-repackageclasses
-allowaccessmodification
-optimizationpasses 5

# Remove logging
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
}
