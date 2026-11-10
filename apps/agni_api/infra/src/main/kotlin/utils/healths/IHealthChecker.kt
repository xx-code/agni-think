package utils.healths

import java.time.Instant

data class HealthReport(
    val name: String,
    val status: HealthStatus,
    val description: String,
    val timestamp: Instant = Instant.now(),
    val error: Throwable? = null,
    val metrics: Map<String, Any> = emptyMap()
)

data class ModuleHealthSummary(
    val moduleName: String,
    val overallStatus: HealthStatus,
    val checks: List<HealthReport>,
    val durationMs: Long
)

interface IHealthChecker {
    val moduleName: String
    suspend fun runCheckHealth(): ModuleHealthSummary
}
