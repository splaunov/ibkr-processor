plugins {
    id("common-conventions")
    kotlin("kapt")
    kotlin("plugin.allopen")
    id("com.github.johnrengelman.shadow") version PluginVersions.SHADOW
    id("io.micronaut.application") version PluginVersions.MICRONAUT
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
    kapt(platform(Micronaut.bom))
    kapt(Other.picoCliCodeGen)
    kapt(Micronaut.inject)
    implementation(Other.picoCli)
    implementation(Micronaut.picoCli)
    implementation(kotlin("reflect"))

    implementation(project(":reader"))
    implementation(project(":exporter"))
}

application {
    mainClass.set("me.splaunov.ibkrprocessor.cli.ApplicationKt")
}