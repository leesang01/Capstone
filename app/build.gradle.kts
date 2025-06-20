plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.googleGmsGoogleServices)
    // alias(libs.plugins.kotlinAndroid) ❌ Kotlin 제거
}

android {
    namespace = "com.example.ecochallengeapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ecochallengeapp"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // kotlinOptions 블록 제거됨 ❌
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase
    implementation(libs.firebase.database)
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Kakao 로그인
    implementation("com.kakao.sdk:v2-user:2.18.0")

    // ExifInterface (사진 회전 문제 해결용)
    implementation("androidx.exifinterface:exifinterface:1.3.6")

    // 아래는 Kotlin 전용 라이브러리이므로 제거됨 ❌
    // implementation(libs.core.ktx)

    // 테스트 관련
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}