plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "de.expatunity.core.testing"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.androidx.core.ktx)
    implementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}