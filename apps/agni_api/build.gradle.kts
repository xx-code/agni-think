plugins {
    // Declarees ici, appliquees dans les modules : une seule source de verite pour les
    // versions, et pas de conflit quand deux modules demandent le meme plugin.
    kotlin("jvm") version "2.2.21" apply false
    kotlin("plugin.spring") version "2.2.21" apply false
    id("org.springframework.boot") version "4.0.2" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    group = "dev.auguste"
    version = "0.0.1-SNAPSHOT"
}

// Versions partagees par les modules, pour eviter qu'un module embarque une
// version divergente (c'etait le cas du BOM Spring Boot).
extra["springBootVersion"] = "4.0.2"
extra["mockkVersion"] = "1.14.9"
extra["postgresqlVersion"] = "42.7.9"
extra["kotlinCoroutinesVersion"] = "1.11.0"

// La racine n'applique aucun plugin JVM : elle n'a donc pas de taches `classes` /
// `testClasses` propres. On expose des agregats pour que `:classes` reste valide
// (run-configurations IDE, scripts) et compile bien les trois modules.
listOf("classes", "testClasses").forEach { name ->
    tasks.register(name) {
        group = "build"
        description = "Aggregate task delegating '$name' to all modules."
        dependsOn(subprojects.map { "${it.path}:$name" })
    }
}