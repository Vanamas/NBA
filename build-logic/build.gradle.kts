plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "courtflow.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
    }
}
