plugins {
    id("common-conventions")
    id("com.google.devtools.ksp") version PluginVersions.KSP
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
    implementation(Other.picoCli)
    implementation(Micronaut.picoCli)
    implementation(kotlin("reflect"))
    runtimeOnly(Micronaut.snakeYaml)

    implementation(project(":reader"))
    implementation(project(":exporter"))
}

application {
    mainClass.set("me.splaunov.ibkrprocessor.cli.ApplicationKt")
}