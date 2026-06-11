package dev.auguste.agni_api

import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import javax.sql.DataSource

@SpringBootApplication
@EnableScheduling
class AgniApiApplication

@Configuration
class CorsConfig(
    @Value("\${origin.frontend.url:http://localhost:8000}") val frontendOrigin: String
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        // TODO: Get allowed origins
        registry.addMapping("/**")
            .allowedOrigins(frontendOrigin)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("Authorization")
            .allowCredentials(true)
            .maxAge(3600)
    }
}

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
