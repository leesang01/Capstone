// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.googleGmsGoogleServices) apply false
    // alias(libs.plugins.kotlinAndroid) apply false ❌ 제거됨
}
