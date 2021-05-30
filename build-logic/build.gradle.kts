plugins {
    `kotlin-dsl`
}

group = "me.splaunov.ibkrprocessor"
version = "0.1"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins.register("me.splaunov.kotlin.conventions") {
        id = "me.splaunov.kotlin.conventions"
        implementationClass = "me.splaunov.ibkrprocessor.gradle.KotlinConventionsPlugin"
    }
    plugins.register("me.splaunov.micronaut.library.conventions") {
        id = "me.splaunov.micronaut.library.conventions"
        implementationClass = "me.splaunov.ibkrprocessor.gradle.MicronautLibraryPlugin"
    }
    plugins.register("me.splaunov.micronaut.application.conventions") {
        id = "me.splaunov.micronaut.application.conventions"
        implementationClass = "me.splaunov.ibkrprocessor.gradle.MicronautApplicationPlugin"
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    implementation("io.micronaut.gradle:micronaut-gradle-plugin:1.5.0")
    runtimeOnly("org.jetbrains.kotlin:kotlin-allopen:1.5.10")
    runtimeOnly("gradle.plugin.com.github.jengelman.gradle.plugins:shadow:7.0.0")
}