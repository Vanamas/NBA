plugins {
    id("courtflow.android.library")
    alias(libs.plugins.google.devtools.ksp)
}

android {
    namespace = "cz.vanama.courtflow.domain"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.paging.common)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.android)
    implementation(project(":core:common"))

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotest.assertions.core)
}
