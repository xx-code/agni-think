package dev.auguste.mcp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = [
        "dev.auguste.mcp",
        "persistences",
        "usecase_configs",
        "configs"
    ]
)
class McpApplication

fun main(args: Array<String>) {
    runApplication<McpApplication>(*args)
}
