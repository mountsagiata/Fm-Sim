plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.mountsa.fmsimulation"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mountsa.fmsimulation"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
    arg("dagger.hilt.disableModulesHaveInstallInCheck", "true")
}

dependencies {
    // =====================================================
    // CORE
    // =====================================================
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.multidex)

    // =====================================================
    // COMPOSE
    // =====================================================
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.lifecycle.runtime.compose)
    // Compose debugging tools
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // =====================================================
    // NAVIGATION
    // =====================================================
    implementation(libs.androidx.navigation.compose)

    // =====================================================
    // VIEWMODEL
    // =====================================================
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // =====================================================
    // HILT (Dependency Injection)
    // =====================================================
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    // =====================================================
    // WORK MANAGER
    // =====================================================
    implementation(libs.androidx.work.runtime.ktx)

    // =====================================================
    // ROOM DATABASE
    // =====================================================
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.sqlite.ktx)
    implementation(libs.sqlcipher)

    // =====================================================
    // SERIALIZATION
    // =====================================================
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.gson)

    // =====================================================
    // COROUTINES
    // =====================================================
    implementation(libs.kotlinx.coroutines.android)

    // =====================================================
    // IMAGE LOADING
    // =====================================================
    implementation(libs.coil.compose)

    // =====================================================
    // DATASTORE
    // =====================================================
    implementation(libs.androidx.datastore.preferences)

    // =====================================================
    // MATERIAL DESIGN
    // =====================================================
    implementation(libs.google.material)

    // =====================================================
    // TESTING
    // =====================================================
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
