package me.splaunov.ibkrprocessor.gradle

import io.micronaut.gradle.MicronautExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

@Suppress("unused")
class MicronautLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply(MicronautPlugin::class.java)
        target.pluginManager.apply("io.micronaut.library")

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