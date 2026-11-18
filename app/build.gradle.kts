plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
}

/*
 * ---------------------------------------------------------
 * Backend URLs
 * ---------------------------------------------------------
 * No trailing slash: HttpSangfroidApi builds "$baseUrl$path".
 * Use the local backend with:
 *   .\gradlew installDebug -Psangfroid.useLocalBackend=true
 * (requires: adb reverse tcp:8080 tcp:8080)
 */

val awsBackendUrl =
    "http://sangfroid-alb-1161678354.ap-southeast-1.elb.amazonaws.com"

val localBackendUrl =
    "http://localhost:8080"

val useLocalBackend =
    (project.findProperty("sangfroid.useLocalBackend") as String?)
        ?.toBoolean()
        ?: false

android {
    namespace = "com.julesmeister.sangfroid"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.julesmeister.sangfroid"

        minSdk = 26
        targetSdk = 37

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "SANGFROID_API_BASE_URL",
                "\"${if (useLocalBackend) localBackendUrl else awsBackendUrl}\""
            )
        }

        release {
            isMinifyEnabled = false

            buildConfigField(
                "String",
                "SANGFROID_API_BASE_URL",
                "\"$awsBackendUrl\""
            )

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_17

        targetCompatibility =
            JavaVersion.VERSION_17
    }

    androidResources {
        noCompress += "onnx"
    }

    packaging {
        resources {
            excludes +=
                "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

room {
    schemaDirectory(
        "$projectDir/schemas"
    )
}

ksp {
    arg(
        "room.generateKotlin",
        "true"
    )
}

dependencies {

    /*
     * ---------------------------------------------------------
     * PDF parsing
     * ---------------------------------------------------------
     */

    implementation(
        "com.tom-roush:pdfbox-android:2.0.27.0"
    )

    /*
     * ---------------------------------------------------------
     * Sherpa ONNX
     * ---------------------------------------------------------
     */

    implementation(
        files(
            "libs/sherpa-onnx-2.0.1.aar"
        )
    )

    /*
     * ---------------------------------------------------------
     * Android core
     * ---------------------------------------------------------
     */

    implementation(
        libs.androidx.core.ktx
    )

    /*
     * ---------------------------------------------------------
     * Lifecycle
     * ---------------------------------------------------------
     */

    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )

    implementation(
        libs.androidx.lifecycle.runtime.compose
    )

    implementation(
        libs.androidx.lifecycle.viewmodel.compose
    )

    /*
     * ---------------------------------------------------------
     * Compose activity + navigation
     * ---------------------------------------------------------
     */

    implementation(
        libs.androidx.activity.compose
    )

    implementation(
        libs.androidx.navigation.compose
    )

    /*
     * ---------------------------------------------------------
     * Jetpack Compose
     * ---------------------------------------------------------
     */

    implementation(
        platform(
            libs.androidx.compose.bom
        )
    )

    implementation(
        libs.androidx.compose.ui
    )

    implementation(
        libs.androidx.compose.ui.graphics
    )

    implementation(
        libs.androidx.compose.ui.tooling.preview
    )

    implementation(
        libs.androidx.compose.material3
    )

    /*
     * ---------------------------------------------------------
     * Room
     * ---------------------------------------------------------
     */

    implementation(
        libs.androidx.room.runtime
    )

    implementation(
        libs.androidx.room.ktx
    )

    ksp(
        libs.androidx.room.compiler
    )

    /*
     * ---------------------------------------------------------
     * Hilt
     * ---------------------------------------------------------
     */

    implementation(
        libs.google.hilt.android
    )

    ksp(
        libs.google.hilt.compiler
    )

    implementation(
        libs.androidx.hilt.lifecycle.viewmodel.compose
    )

    /*
     * ---------------------------------------------------------
     * Unit tests
     * ---------------------------------------------------------
     */

    testImplementation(
        libs.junit
    )

    testImplementation(
        libs.androidx.room.testing
    )

    /*
     * ---------------------------------------------------------
     * Instrumented tests
     * ---------------------------------------------------------
     */

    androidTestImplementation(
        libs.androidx.junit
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        platform(
            libs.androidx.compose.bom
        )
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    /*
     * ---------------------------------------------------------
     * Debug Compose tooling
     * ---------------------------------------------------------
     */

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )

    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )
}