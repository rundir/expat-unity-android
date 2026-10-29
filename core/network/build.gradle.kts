// core/network/build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}
android {
    namespace = "de.expatunity.core.network"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    buildFeatures { buildConfig = true }
    buildTypes {
        debug { buildConfigField("String", "BASE_URL", "\"http://localhost:1337/api/mobile/v1/\"") }
        release { buildConfigField("String", "BASE_URL", "\"https://www.expat-unity.de/cms/api/mobile/v1/\"") }
    }
}
dependencies {
    api(project(":core:domain"))
    implementation(project(":core:domain"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}