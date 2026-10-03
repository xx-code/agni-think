package persistences

import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories

/**
 * Les interfaces `*Storage` sont des `CrudRepository` (Spring Data), mais l'application est
 * declaree dans `dev.auguste.rest_api` : l'auto-configuration de Spring Boot ne cherche les
 * repositories que depuis le package de `@SpringBootApplication`, elle ne trouvait donc aucun
 * bean `AccountStorage`, `BudgetStorage`...
 *
 * `GenericStorage` est `@NoRepositoryBean` : seules les interfaces concretes sont implementees.
 */
@Configuration
@EnableJdbcRepositories(basePackages = ["persistences"])
class JdbcRepositoryScanConfig