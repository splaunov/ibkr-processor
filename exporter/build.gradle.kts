plugins {
    id("me.splaunov.micronaut.library.conventions")
}

dependencies {
    implementation("org.apache.poi:poi-ooxml:5.0.0")

    implementation(project(":data"))
}