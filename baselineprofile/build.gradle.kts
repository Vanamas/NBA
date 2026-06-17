import com.android.build.api.dsl.ManagedVirtualDevice

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.androidx.baselineprofile)
}

/**
 * Producer module for the app's Baseline Profile. It is a `com.android.test`
 * module that drives the real [":app"] through a startup + scroll journey on a
 * Gradle Managed Device, records which methods run, and emits the AOT-compile
 * hints consumed by `:app` (see its `baselineProfile(project(":baselineprofile"))`
 * dependency). [ScrollBenchmark] additionally measures frame timing with and
 * without the profile so the cold-start jank improvement is quantifiable.
 */
android {
    namespace = "cz.vanama.courtflow.baselineprofile"
    compileSdk = 37

    defaultConfig {
        // Baseline Profile collection requires API 28+; 34 keeps the managed
        // device on a recent ART that matches the field.
        minSdk = 28
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // The app under instrumentation.
    targetProjectPath = ":app"

    // A headless emulator so `generateBaselineProfile` needs no attached device.
    testOptions.managedDevices.allDevices {
        create<ManagedVirtualDevice>("pixel6Api34") {
            device = "Pixel 6"
            apiLevel = 34
            systemImageSource = "aosp"
        }
    }
}

baselineProfile {
    managedDevices += "pixel6Api34"
    // Generate on the managed emulator in CI; flip to true to use a plugged-in
    // device locally (faster, and the only option on machines without KVM).
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}
