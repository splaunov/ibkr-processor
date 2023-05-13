plugins {
    id("common-conventions")
    kotlin("kapt")
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
    kapt(platform(Micronaut.bom))
    kapt(Micronaut.inject)
    implementation(Micronaut.runtime)
    implementation(Micronaut.kotlinRuntime)
    implementation(Other.annotationApi)
    implementation(Micronaut.extensionFunctions)

    implementation(Other.poiOoxml)

    implementation(project(":data"))
    implementation(project(":reader"))

    // Testing
    testImplementation(Kotest.assertions)
    testImplementation(Other.mockk)
    testImplementation(JUnit.jupiterParams)
}