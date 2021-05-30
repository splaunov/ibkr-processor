plugins {
    id("me.splaunov.micronaut.application.conventions")
}

dependencies {
    kapt("info.picocli:picocli-codegen:4.6.1")
    implementation("info.picocli:picocli")
    implementation("io.micronaut.picocli:micronaut-picocli")

    implementation(project(":reader"))
    implementation(project(":exporter"))
}

application {
    mainClass.set("me.splaunov.ibkrprocessor.cli.Application")
}