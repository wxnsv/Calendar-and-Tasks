import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs.kotlin")
    id("org.jetbrains.kotlin.android") apply false
}

android {
    namespace = "com.nikkap.calendar.app"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.nikkap.calendar"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties().apply {
            val propertiesFile = rootProject.file("local.properties")
            if (propertiesFile.exists()) {
                load(propertiesFile.inputStream())
            }
        }
        val googleClientId = properties.getProperty("GOOGLE_CLIENT_ID") ?: "\"\""

        buildConfigField("String", "GOOGLE_CLIENT_ID", googleClientId)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    //noinspection WrongGradleMethod
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
    configurations.all {
        exclude(group = "com.intellij", module = "annotations")
        exclude(group = "org.jetbrains", module = "annotations-java5")
    }
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }
    sourceSets {
        getByName("main") {
            res.directories.add(
                "src/main/res"
            )
        }
    }
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

dependencies {
// Project
    implementation(project(":domain"))
    implementation(project(":core"))
    implementation(project(":data"))
// Coil
    implementation(libs.coil)
// Work
    implementation(libs.androidx.work.runtime.ktx)
// Nav
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
// XML
    implementation(libs.androidx.swiperefreshlayout)
// Preferences
    implementation(libs.androidx.datastore.preferences)
// Material
    implementation(libs.material)
// Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.googleid)
// SplashScreen
    implementation(libs.androidx.core.splashscreen)
// Retrofit
    implementation(libs.retrofit)
// Moshi
    implementation(libs.moshi.kotlin)
    implementation(libs.converter.moshi)
// OkHttp
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
// Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    testImplementation(libs.kotlinx.coroutines.test)
// Credentials
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
// Google Play Services Auth
    implementation(libs.play.services.auth)
    implementation(libs.google.api.services.tasks)
    implementation(libs.google.api.services.calendar)
// Google API Client
    implementation(libs.google.api.client.android)
// Koin
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.android)
    implementation("io.insert-koin:koin-androidx-workmanager:3.5.0")
// Unit Test
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockk)

    implementation("com.kizitonwose.calendar:compose:2.10.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}