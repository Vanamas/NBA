import java.net.URI
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.openapi.generator)
}

val localProperties =
    Properties().apply {
        val propertiesFile = rootProject.file("local.properties")
        if (propertiesFile.exists()) {
            propertiesFile.inputStream().use { load(it) }
        }
    }

android {
    namespace = "cz.vanama.courtflow.core.network"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField(
            "String",
            "BALLDONTLIE_API_KEY",
            "\"${localProperties.getProperty("balldontlie.apiKey", "")}\"",
        )

        // The generated endpoint paths already carry the league/version prefix
        // (`nba/v1/...`), so the base URL is the bare host. Build types and
        // flavors can override this field to point at a different environment.
        buildConfigField(
            "String",
            "BALLDONTLIE_BASE_URL",
            "\"https://api.balldontlie.io/\"",
        )
    }

    buildFeatures {
        buildConfig = true
    }

    sourceSets {
        getByName("main") {
            // Task dependencies are wired explicitly below (AGP rejects Provider here).
            kotlin.directories.add("build/generated/openapi/src/main/kotlin")
        }
    }
}

// The Retrofit API interface and Moshi models are generated from the official
// balldontlie OpenAPI definition (snapshot of https://www.balldontlie.io/openapi/nba.yml,
// see https://www.balldontlie.io/blog/ai-assisted-development/); never edit the output.
openApiGenerate {
    generatorName.set("kotlin")
    cleanupOutput.set(true)
    inputSpec.set(
        layout.projectDirectory
            .file("openapi/nba.yml")
            .asFile.absolutePath,
    )
    outputDir.set(
        layout.buildDirectory
            .dir("generated/openapi")
            .get()
            .asFile.absolutePath,
    )
    packageName.set("cz.vanama.courtflow.core.network.generated")
    apiPackage.set("cz.vanama.courtflow.core.network.generated.api")
    modelPackage.set("cz.vanama.courtflow.core.network.generated.model")
    library.set("jvm-retrofit2")
    configOptions.set(
        mapOf(
            "serializationLibrary" to "moshi",
            "moshiCodeGen" to "true",
            "useCoroutines" to "true",
            // Suspend functions return the body directly and throw HttpException
            // on non-2xx, matching the error handling in the data layer.
            "useResponseAsReturnType" to "false",
            // java.time needs API 26+ (minSdk is 24); dates stay plain strings.
            "dateLibrary" to "string",
        ),
    )
    generateApiDocumentation.set(false)
    generateModelDocumentation.set(false)
    generateApiTests.set(false)
    generateModelTests.set(false)
}

// Refreshes the committed spec snapshot from the official source. Builds keep
// using the committed file so they stay deterministic and work offline; run
// this task (and review the diff) to pick up upstream API changes.
tasks.register("updateApiSpec") {
    group = "openapi tools"
    description = "Downloads the latest official OpenAPI definition into openapi/nba.yml."
    val specFile = layout.projectDirectory.file("openapi/nba.yml").asFile
    doLast {
        specFile.writeText(URI("https://www.balldontlie.io/openapi/nba.yml").toURL().readText())
    }
}

tasks
    .matching {
        it.name.startsWith("compile") || it.name.startsWith("ksp") || it.name.startsWith("runKtlint")
    }.configureEach {
        dependsOn(tasks.named("openApiGenerate"))
    }

// Static analysis is for hand-written code only; the generated client is
// excluded (detekt's default source dirs never include build/, ktlint's do).
configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    filter {
        exclude { it.file.path.contains("generated") }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    // Required by the generated infrastructure (ApiClient).
    implementation(libs.converter.scalars)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.koin.android)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
}
