package me.splaunov.ibkrprocessor.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("unused")
class KotlinConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("org.jetbrains.kotlin.jvm")

        target.extensions.configure<JavaPluginExtension>("java") {
            sourceCompatibility = JavaVersion.toVersion("11")
        }

        (target.tasks["compileKotlin"] as KotlinCompile).kotlinOptions.jvmTarget = "11"
        (target.tasks["compileTestKotlin"] as KotlinCompile).kotlinOptions.jvmTarget = "11"

        target.dependencies {
            "runtimeOnly"("ch.qos.logback:logback-classic")

            "implementation"(platform("org.jetbrains.kotlin:kotlin-bom"))
            "implementation"("org.jetbrains.kotlin:kotlin-stdlib")

            "testImplementation"("org.junit.jupiter:junit-jupiter:5.7.2")
            "testImplementation"("io.kotest:kotest-assertions-core:4.5.0")
            "testImplementation"("io.mockk:mockk:1.11.0")
        }
    }
}