plugins {
    java
    id("com.diffplug.spotless") version "8.10.4"
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.diegodev"
version = "0.0.1-SNAPSHOT"
description = "MrCatFood"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.34.0").aosp()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("1.8.0")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

val podmanSocket = System.getenv("XDG_RUNTIME_DIR")?.let { "$it/podman/podman.sock" }
val dockerHost =
    System.getenv("DOCKER_HOST")
        ?: podmanSocket?.takeIf { file(it).exists() }?.let { "unix://$it" }

tasks.withType<Test> {
    useJUnitPlatform()
    dockerHost?.let { environment("DOCKER_HOST", it) }
    environment("TESTCONTAINERS_RYUK_DISABLED", "true")
}

tasks.test {
    useJUnitPlatform { excludeTags("integracion") }
}

val integracionTest =
    tasks.register<Test>("integrationTest") {
        description = "Ejecuta las pruebas de integración sobre PostgreSQL efímero."
        group = "verification"
        testClassesDirs =
            sourceSets.test
                .get()
                .output.classesDirs
        classpath = sourceSets.test.get().runtimeClasspath
        useJUnitPlatform { includeTags("integracion") }
        shouldRunAfter(tasks.test)
    }
