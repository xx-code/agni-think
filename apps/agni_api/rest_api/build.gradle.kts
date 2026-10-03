plugins {
    kotlin("jvm")
    // Ouvre les classes @Configuration / @Component : sans cela Spring Boot 4 refuse
    // les classes Kotlin finales ("@Configuration class 'FlywayConfig' may not be final").
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "rest_api"

dependencies {
    implementation(project(":core"))
    // transitif via `api` de `:infra`, mais declare explicitement car l'application
    // utilise directement les types du domaine.
    implementation(project(":infra"))

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:${rootProject.extra["mockkVersion"]}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    // Obligatoire : sans cela ce module compilait avec le JDK du daemon Gradle au
    // lieu du toolchain du projet (=> UnsupportedClassVersionError au lancement).
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}