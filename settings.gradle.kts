rootProject.name = "ibkr-processor"
include("reader")
include("cli")
include("exporter")
include("data")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
