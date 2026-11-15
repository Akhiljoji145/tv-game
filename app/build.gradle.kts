plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.akhil.classicgamestv"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.akhil.classicgamestv"
        minSdk = 23
        targetSdk = 31
        versionCode = 2
        versionName = "2.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
