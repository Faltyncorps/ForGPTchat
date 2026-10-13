plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.compose")
}

android {
    namespace = "cz.faltyn.elfall.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "cz.faltyn.elfall"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "0.5.0"
    }

    buildFeatures { compose = true; buildConfig = true }

    flavorDimensions += listOf("app", "network")
    productFlavors {
        // Eleanor = standalone wallet: direct public testnet APIs, no RunPod / ELFALL network, no FLC / cash-out / swap.
        create("eleanor") {
            dimension = "app"
            isDefault = true
            applicationId = "cz.faltyn.eleanor"
            resValue("string", "app_name", "Eleanor")
            buildConfigField("boolean", "STANDALONE", "true")
        }
        create("elfall") {
            dimension = "app"
            resValue("string", "app_name", "ELFALL")
            buildConfigField("boolean", "STANDALONE", "false")
        }
        create("bitcoinTestnet") {
            dimension = "network"
            isDefault = true
            applicationIdSuffix = ".testnet"
            buildConfigField("String", "BTC_NETWORK", "\"TESTNET\"")
        }
        create("bitcoinMainnet") {
            dimension = "network"
            buildConfigField("String", "BTC_NETWORK", "\"MAINNET\"")
        }
    }

    buildTypes {
        release {
            // R8: shrink + obfuscate release builds (debug/CI builds unaffected)
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += setOf("META-INF/{AL2.0,LGPL2.1}", "META-INF/versions/9/previous-compilation-data.bin")
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("fr.acinq.bitcoin:bitcoin-kmp:0.32.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    // QR scan (camera intent, handles CameraX/permissions internally) + QR render
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.3")
}

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
