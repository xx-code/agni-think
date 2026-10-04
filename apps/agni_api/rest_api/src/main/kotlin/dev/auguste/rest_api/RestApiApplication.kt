package dev.auguste.rest_api

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import javax.sql.DataSource

@SpringBootApplication(
    scanBasePackages = [
        "dev.auguste.rest_api",
        "persistences",
        "usecase_configs",
        "configs"
    ]
)
@EnableScheduling
class RestApiApplication

@Configuration
class FlywayConfig(private val dataSource: DataSource) {
    @Bean(initMethod = "migrate")
    fun flyway(): Flyway {
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load()
        return flyway
    }
}

fun main(args: Array<String>) {
    runApplication<RestApiApplication>(*args)
}
