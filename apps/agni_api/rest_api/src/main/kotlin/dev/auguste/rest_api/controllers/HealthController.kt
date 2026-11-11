package dev.auguste.rest_api.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import utils.healths.HealthCheckerAggregator
import utils.healths.HealthStatus

@RestController
@RequestMapping("/health")
class HealthController(private val healthChecker: HealthCheckerAggregator) {

    @GetMapping
    suspend fun health(): ResponseEntity<Map<String, Any>> {
        val report = healthChecker.runAllCheckHealth()
        val status = report["status"]

        return ResponseEntity
            .status(httpStatus(status))
            .body(report)
    }

    @GetMapping("/ready")
    suspend fun ready(): ResponseEntity<Map<String, Any>> {
        val ready = healthChecker.isReady()

        return ResponseEntity
            .status(if (ready) HttpStatus.OK else HttpStatus.SERVICE_UNAVAILABLE)
            .body(mapOf("status" to if (ready) "READY" else "NOT_READY"))
    }

    @GetMapping("/liveness")
    suspend fun liveness(): ResponseEntity<Map<String, Any>> {
        val healthy = healthChecker.isHealthy()

        return ResponseEntity
            .status(if (healthy) HttpStatus.OK else HttpStatus.SERVICE_UNAVAILABLE)
            .body(mapOf("status" to if (healthy) "ALIVE" else "DOWN"))
    }

    private fun httpStatus(status: Any?): HttpStatus =
        if (status == HealthStatus.UNHEALTHY) HttpStatus.SERVICE_UNAVAILABLE else HttpStatus.OK
}
