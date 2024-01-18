plugins {
    id("com.android.application")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.ruchitech.quicklinkcaller"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ruchitech.quicklinkcaller"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("com.google.firebase:firebase-messaging:23.4.0")
    implementation("com.google.firebase:firebase-crashlytics:18.6.0")
    implementation("com.google.firebase:firebase-analytics:21.5.0")
    val compose_version = "1.5.4"
    val nav_version = "2.7.6"
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-service:2.5.1")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.navigation:navigation-compose:$nav_version")
    implementation ("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.compose.ui:ui:$compose_version")
    implementation("androidx.compose.ui:ui-graphics:$compose_version")
    implementation("androidx.compose.ui:ui-tooling-preview:$compose_version")
    debugImplementation("androidx.compose.ui:ui-tooling:$compose_version")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$compose_version")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$compose_version")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.lifecycle:lifecycle-service:2.6.2")
    implementation("com.google.dagger:hilt-android:2.48.1")
    kapt("com.google.dagger:hilt-android-compiler:2.48.1")
    implementation("com.auth0:java-jwt:3.18.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.github.dawidraszka.compose-permission-handler:core:1.5.0")
    // Only required if you want to use utils package
    implementation("com.github.dawidraszka.compose-permission-handler:utils:1.5.0")
    // Room
    val room_version = "2.6.0"
    implementation ("androidx.room:room-ktx:$room_version")
    kapt ("androidx.room:room-compiler:$room_version")
    implementation ("androidx.room:room-runtime:$room_version")
    annotationProcessor ("androidx.room:room-compiler:$room_version")

    // dialog sheets
    implementation ("com.maxkeppeler.sheets-compose-dialogs:core:1.0.4")
    implementation ("com.maxkeppeler.sheets-compose-dialogs:input:1.0.4")
    implementation ("com.maxkeppeler.sheets-compose-dialogs:state:1.0.4")
    implementation ("com.maxkeppeler.sheets-compose-dialogs:clock:1.0.4")
    implementation ("com.maxkeppeler.sheets-compose-dialogs:calendar:1.0.4")

    //gson
    implementation ("com.google.code.gson:gson:2.10.1")
}