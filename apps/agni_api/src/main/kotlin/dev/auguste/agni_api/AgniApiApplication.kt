package dev.auguste.agni_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AgniApiApplication

fun main(args: Array<String>) {
    runApplication<AgniApiApplication>(*args)
}
