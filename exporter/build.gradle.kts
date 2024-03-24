plugins {
    id("common-conventions")
    id("com.google.devtools.ksp") version PluginVersions.KSP
    kotlin("plugin.allopen")
    id("io.micronaut.library") version PluginVersions.MICRONAUT
}

micronaut {
    version(Versions.MICRONAUT)
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("me.splaunov.ibkrprocessor.*")
    }
}

dependencies {
    // Micronaut Core
    implementation(Micronaut.runtime)
    implementation(Micronaut.kotlinRuntime)
    implementation(Other.annotationApi)
    implementation(Micronaut.extensionFunctions)

    implementation(Other.poiOoxml)

    implementation(project(":data"))
    implementation(project(":reader"))

    // Logging
    implementation(Logging.logback)
    implementation(Logging.kotlinLogging)

    // Testing
    testImplementation(Kotest.assertions)
    testImplementation(Other.mockk)
    testImplementation(JUnit.jupiterParams)
}