plugins {
    id("java")
    id("io.gatling.gradle") version "3.12.0.3"
}

group = "com.felipejdias"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17) // projeto em scala da rinha nao funciona com java 21
    }
}