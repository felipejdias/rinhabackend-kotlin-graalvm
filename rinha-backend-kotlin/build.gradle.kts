plugins {
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.graalvm.buildtools.native") version "0.10.2"
    kotlin("plugin.jpa") version "1.9.24"
}

group = "com.felipejdias"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2023.0.3"

dependencies {

    //kotlin
    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect")

    //spring
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("com.zaxxer:HikariCP:5.1.0")


    //postgress
    implementation("org.postgresql:postgresql")

    // https://mvnrepository.com/artifact/org.graalvm.nativeimage/svm
    compileOnly("org.graalvm.nativeimage:svm:24.0.2")


}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
