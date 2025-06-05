plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.googleGmsGoogleServices)
    alias(libs.plugins.kotlinAndroid) // 이미 있음 ✅
}

android {
    namespace = "com.example.ecochallengeapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ecochallengeapp"
        minSdk = 24
        targetSdk = 35.toInt()
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // 기존
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation("com.kakao.sdk:v2-user:2.18.0")

    // 🔹 추가: Firebase 인증
    implementation("com.google.firebase:firebase-auth:22.3.1")

    // 🔹 추가: Google 로그인
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // ✅ Kakao 로그인 추가
    implementation("com.kakao.sdk:v2-user:2.18.0")
    implementation(libs.core.ktx)
    // 테스트 관련
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
