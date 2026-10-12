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
        versionCode = 2
        versionName = "0.2.0"
    }

    buildFeatures { compose = true; buildConfig = true }

    flavorDimensions += "network"
    productFlavors {
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
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
}
