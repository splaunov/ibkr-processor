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

tasks {
    test {
        useJUnitPlatform()
    }
}

dependencies {
    // Micronaut Core
    kapt(platform(Micronaut.bom))
    kapt(Micronaut.inject)
    implementation(Micronaut.runtime)
    implementation(Micronaut.kotlinRuntime)

    implementation(Micronaut.jacksonXml)
    implementation(Micronaut.httpClient)

    implementation(Other.kotlinCsv)

    implementation(project(":data"))

    // Testing
    testImplementation(Kotest.assertions)
    testImplementation(Other.mockk)
    testImplementation(JUnit.jupiterParams)
}