package me.splaunov.ibkrprocessor.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

@Suppress("unused")
class MicronautPlugin : Plugin<Project> {
    override fun apply(target: Project) {

        target.pluginManager.apply(KotlinConventionsPlugin::class.java)
        target.pluginManager.apply("org.jetbrains.kotlin.plugin.allopen")
        target.pluginManager.apply("org.jetbrains.kotlin.kapt")

        target.dependencies {
            "kapt"("io.micronaut:micronaut-inject-java:2.5.3")
            "kaptTest"("io.micronaut:micronaut-inject-java:2.5.3")
        }

        target.extensions.configure<KaptExtension>("kapt") {
            arguments {
                arg("micronaut.processing.incremental", true)
            }
        }
    }
}