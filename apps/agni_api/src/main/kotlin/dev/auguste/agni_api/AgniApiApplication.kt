package dev.auguste.agni_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class AgniApiApplication

fun main(args: Array<String>) {
    runApplication<AgniApiApplication>(*args)
}
