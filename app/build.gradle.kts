plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.goalmate"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.goalmate"
        minSdk = 24
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
    }
    lint {
        checkDependencies = true
        checkReleaseBuilds = false
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
    implementation ("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.material:material:1.5.0")
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.storage)
    implementation(libs.play.services.cast.tv)
    implementation(libs.firebase.messaging) // FCM
    implementation(platform(libs.firebase.bom))
    implementation(libs.volley)
    implementation(libs.androidx.tools.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation ("com.airbnb.android:lottie-compose:6.2.0")


    implementation("com.android.volley:volley:1.2.1")


    implementation ("com.cloudinary:cloudinary-android:2.3.1") // resimlerimizin depolama alanı olarak kullanacağız

    implementation( "androidx.room:room-runtime:2.6.1")
    kapt ("androidx.room:room-compiler:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1")


    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") // Flow'un temel kütüphanesi
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // Android için Coroutine desteği

    implementation("io.coil-kt:coil-compose:2.4.0") // resimleri yüklemek için

    implementation ("com.google.code.gson:gson:2.10.1")
    implementation("com.google.dagger:hilt-android:2.51.1")
    implementation ("androidx.work:work-runtime-ktx:2.8.1")// WorkManager
    implementation ("androidx.hilt:hilt-work:1.2.0") // workManager
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    //(durum çubugu rengi vb)
    implementation ("com.google.accompanist:accompanist-systemuicontroller:0.28.0")


    implementation( "com.airbnb.android:lottie-compose:6.3.0")

    // API 33 ve üzeri
    implementation( libs.accompanist.permissions)


}