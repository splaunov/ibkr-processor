package me.splaunov.ibkrprocessor.gradle

import io.micronaut.gradle.MicronautExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.version
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

@Suppress("unused")
class MicronautApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply(MicronautPlugin::class.java)
        target.pluginManager.apply("io.micronaut.application")
        target.pluginManager.apply("com.github.johnrengelman.shadow")

        target.extensions.configure<MicronautExtension>("micronaut") {
            testRuntime("junit5")
        }
//        target.extensions.configure<KaptExtension>("kapt") {
//            arguments {
//                arg("micronaut.processing.incremental", true)
//            }
//        }
    }
}