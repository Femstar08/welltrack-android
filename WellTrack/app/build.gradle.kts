plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")

}

android {
    namespace = "com.beaconledger.welltrack"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.beaconledger.welltrack"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Supabase configuration
        buildConfigField("String", "SUPABASE_URL", "\"https://nppjffhzkzfduulbbcih.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5wcGpmZmh6a3pmZHV1bGJiY2loIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTAyMjQ2MTAsImV4cCI6MjA2NTgwMDYxMH0.OrwLcR8sXcsyMUVEAXgw2WNureeAKrwgrhrPGT6lgTU\"")
        
        // Enable 16KB page size support for Android 15+
        // Exclude x86_64 due to ML Kit 16KB alignment issues
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
        
        // Add 16KB page size support flag
        manifestPlaceholders["supportsLargeHeap"] = "true"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        // Handle 16KB page size alignment for native libraries
        jniLibs {
            useLegacyPackaging = false
            // Ensure proper alignment for 16KB page sizes
            pickFirsts += listOf("**/libc++_shared.so", "**/libimage_processing_util_jni.so")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    
    // Additional Testing Dependencies
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.robolectric:robolectric:4.11.1")
    
    // Android Test Dependencies
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:truth:1.5.0")
    androidTestImplementation("org.mockito:mockito-android:5.7.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    // Hilt Testing
    testImplementation("com.google.dagger:hilt-android-testing:2.48")
    kspTest("com.google.dagger:hilt-compiler:2.48")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48")
    kspAndroidTest("com.google.dagger:hilt-compiler:2.48")
    
    // Performance Testing
    androidTestImplementation("androidx.benchmark:benchmark-junit4:1.2.2")
    
    // Security Testing
    testImplementation("org.bouncycastle:bcprov-jdk15on:1.70")
    // Hilt for Dependency Injection
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

// Room for Local Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    // ksp("androidx.room:room-compiler:2.6.1") // Temporarily disabled

// Retrofit for Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Supabase SDK
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.6.0")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.6.0")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.6.0")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.6.0")
    
    // Ktor HTTP Client (required for Supabase)
    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-client-okhttp:2.3.12")

// Health Connect
    implementation("androidx.health.connect:connect-client:1.1.0-rc03")

// Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

// ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

// Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // ML Kit for OCR
    // Note: ML Kit has known 16KB page size alignment issues on x86_64
    // We've excluded x86_64 architecture above to address this
    implementation("com.google.mlkit:text-recognition:16.0.0")
    
    // CameraX for image capture
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    
    // Web scraping
    implementation("org.jsoup:jsoup:1.17.2")
    
    // Permission handling
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

}