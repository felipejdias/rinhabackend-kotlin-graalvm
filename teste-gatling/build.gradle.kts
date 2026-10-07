plugins {
    scala
    id("io.gatling.gradle") version "3.15.1.3"
}

group = "com.felipejdias"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// Copia as dependencias do Gatling para rodar a simulacao com "java -cp" puro (scripts/run-load-test.ps1).
// Com network_mode host o Gradle nao funciona dentro do container (cliente/daemon/workers conversam por
// TCP no loopback, que o Docker Desktop intercepta), entao o Gradle so compila e a JVM do Gatling roda sozinha.
tasks.register<Sync>("gatlingRuntimeLibs") {
    dependsOn("gatlingClasses", "processGatlingResources")
    from(configurations.named("gatlingRuntimeClasspath"))
    into(layout.buildDirectory.dir("gatling-runtime/lib"))
}
