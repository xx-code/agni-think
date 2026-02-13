package dev.auguste.agni_api

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import javax.sql.DataSource

@SpringBootApplication
@EnableScheduling
class AgniApiApplication

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
    runApplication<AgniApiApplication>(*args)
}
