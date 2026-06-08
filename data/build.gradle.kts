plugins {
    alias(libs.plugins.android.library)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.nikkap.calendar.data"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
}

dependencies {
// Project
    implementation(project(":domain"))
    implementation(project(":core"))
// Coil
    implementation(libs.coil)
// Work
    implementation(libs.androidx.work.runtime.ktx)
// Preferences
    implementation(libs.androidx.datastore.preferences)
// Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    androidTestImplementation(libs.room.testing)
// SplashScreen
    implementation(libs.androidx.core.splashscreen)
// Retrofit
    implementation(libs.retrofit)
    implementation(libs.google.http.client.gson)
// OkHttp
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
// Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    testImplementation(libs.kotlinx.coroutines.test)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}