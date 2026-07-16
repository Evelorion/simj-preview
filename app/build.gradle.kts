plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose") }

fun ProviderFactory.gradleOrEnv(propertyName: String, envName: String): String? =
    gradleProperty(propertyName).orElse(environmentVariable(envName)).orNull

fun String.escapedForBuildConfig(): String =
    replace("\\", "\\\\").replace("\"", "\\\"")

val releaseStoreFilePath = providers.gradleOrEnv("simj.signing.storeFile", "SIMJ_SIGNING_STORE_FILE")
val releaseStorePassword = providers.gradleOrEnv("simj.signing.storePassword", "SIMJ_SIGNING_STORE_PASSWORD")
val releaseKeyAlias = providers.gradleOrEnv("simj.signing.keyAlias", "SIMJ_SIGNING_KEY_ALIAS")
val releaseKeyPassword = providers.gradleOrEnv("simj.signing.keyPassword", "SIMJ_SIGNING_KEY_PASSWORD")
val releaseStoreFile = releaseStoreFilePath?.let { rootProject.file(it) }
val hasReleaseSigning = releaseStoreFile?.exists() == true &&
    !releaseStorePassword.isNullOrBlank() &&
    !releaseKeyAlias.isNullOrBlank() &&
    !releaseKeyPassword.isNullOrBlank()

val updateRepoOwner = providers.gradleOrEnv("simj.updateRepoOwner", "SIMJ_UPDATE_REPO_OWNER").orEmpty()
val updateRepoName = providers.gradleOrEnv("simj.updateRepoName", "SIMJ_UPDATE_REPO_NAME").orEmpty()

android { namespace = "com.sansim.app"; compileSdk = 35
    defaultConfig {
        applicationId = "com.sansim.app"; minSdk = 26; targetSdk = 35; versionCode = 3024; versionName = "3.0.24-pre"
        buildConfigField("String", "SIMJ_UPDATE_REPO_OWNER", "\"${updateRepoOwner.escapedForBuildConfig()}\"")
        buildConfigField("String", "SIMJ_UPDATE_REPO_NAME", "\"${updateRepoName.escapedForBuildConfig()}\"")
    }
    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseStoreFile
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }
    buildTypes {
        release { isMinifyEnabled = true; isShrinkResources = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"); if (hasReleaseSigning) signingConfig = signingConfigs.getByName("release") }
        debug { if (hasReleaseSigning) signingConfig = signingConfigs.getByName("release") }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.10.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("sh.calvin.reorderable:reorderable:3.1.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    val cameraxVersion = "1.4.0"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
