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

tasks {
    test {
        useJUnitPlatform()
    }
}

dependencies {
    // Micronaut Core
    implementation(Micronaut.runtime)
    implementation(Micronaut.kotlinRuntime)

    implementation(Micronaut.jacksonXml)
    implementation(Micronaut.httpClient)

    implementation(Other.kotlinCsv)

    implementation(project(":data"))

    // Testing
    testImplementation(Micronaut.kotest)
    testImplementation(Kotest.runner)
    testImplementation(Kotest.assertions)
    testImplementation(Kotest.datatest)
    testImplementation(Other.mockk)
    testImplementation(JUnit.jupiterParams)
}