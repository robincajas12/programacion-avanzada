plugins {
    kotlin("jvm") version "2.3.20"
    application
    id("io.freefair.lombok") version "9.5.0"
    kotlin("plugin.lombok") version "2.4.0"


}

group = "com.avanzada"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.varabyte.kotter:kotter:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}