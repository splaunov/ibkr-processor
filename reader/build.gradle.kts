plugins {
    id("me.splaunov.micronaut.library.conventions")
}

dependencies {
    implementation("io.micronaut:micronaut-http-client:2.5.3")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:0.15.2")
    implementation("io.micronaut.xml:micronaut-jackson-xml")

    implementation(project(":data"))
}