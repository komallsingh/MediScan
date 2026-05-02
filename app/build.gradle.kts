import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val localProps = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) {
    localProps.load(localPropsFile.inputStream())
}

val openAiKey = localProps.getProperty("OPENAI_API_KEY") ?: ""

android {
    namespace = "com.komal.mediscan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.komal.mediscan"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "OPENAI_API_KEY", "\"$openAiKey\"")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "OPENAI_API_KEY", "\"$openAiKey\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    // ── Jetpack Compose ──────────────────────────────────────────────────────
    implementation(platform("androidx.compose:compose-bom:2024.04.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.0")

    // ── Core AndroidX ────────────────────────────────────────────────────────
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // ── Navigation + ViewModel ───────────────────────────────────────────────
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // ── CameraX ──────────────────────────────────────────────────────────────
    implementation("androidx.camera:camera-camera2:1.3.3")
    implementation("androidx.camera:camera-lifecycle:1.3.3")
    implementation("androidx.camera:camera-view:1.3.3")

    // ── ML Kit OCR ───────────────────────────────────────────────────────────
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // ── OkHttp ───────────────────────────────────────────────────────────────
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // ── Coil (image loading) ─────────────────────────────────────────────────
    implementation("io.coil-kt:coil-compose:2.6.0")

    // ── Coroutines ───────────────────────────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // ── Gson ─────────────────────────────────────────────────────────────────
    implementation("com.google.code.gson:gson:2.10.1")

    // ── Accompanist permissions ───────────────────────────────────────────────
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // ── iText7 PDF ───────────────────────────────────────────────────────────
    implementation("com.itextpdf:itext7-core:7.2.5") {
        exclude(group = "org.bouncycastle")
    }

    // ── Tests ────────────────────────────────────────────────────────────────
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.04.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}