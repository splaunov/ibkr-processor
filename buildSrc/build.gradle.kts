plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

val kotlin = "1.9.23"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin")
}