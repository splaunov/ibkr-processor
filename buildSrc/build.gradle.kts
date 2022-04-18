plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

val kotlin = "1.6.10"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin")
}