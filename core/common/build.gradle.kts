plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "cz.vanama.courtflow.core.common"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}
