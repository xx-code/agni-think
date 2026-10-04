pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        kotlin("jvm") version "2.2.21"
        kotlin("plugin.spring") version "2.2.21"
        id("org.springframework.boot") version "4.1.1"
        id("io.spring.dependency-management") version "1.1.7"
    }
}

// Les modules declarent `jvmToolchain(21)`. Sans ce resolveur, Gradle ne peut
// trouver un JDK 21 que s'il est deja installe sur la machine (et le build
// echoue sur CI / poste neuf).
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Source unique des depots pour tous les modules (evite qu'un module oublie de
// declarer `repositories` et casse la resolution de sa compileClasspath).
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "agni_api"

include(":core")
include(":infra")
include(":rest_api")
include(":mcp")