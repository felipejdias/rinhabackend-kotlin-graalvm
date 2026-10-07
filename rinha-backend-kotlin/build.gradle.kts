plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.3.21"
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "1.1.1"
}

group = "com.felipejdias"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {

    //kotlin
    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect")

    //spring
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    //postgress
    implementation("org.postgresql:postgresql")

}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootJar {
    archiveFileName.set("rinha-backend-2024-q1.jar")
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("rinha-backend-2024-q1")
            // sem limite o native-image pega ~75% da RAM e todas as CPUs da VM do Docker Desktop
            // e derruba o daemon do Docker na fase de compilacao
            jvmArgs.add("-Xmx4g")
            buildArgs.add("--parallelism=4")
        }
    }
}
