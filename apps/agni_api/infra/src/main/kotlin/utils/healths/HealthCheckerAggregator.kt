package utils.healths

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class HealthCheckerAggregator(
    private val checkers: List<IHealthChecker>
) {

    suspend fun isHealthy(): Boolean =
        collectSummaries().any { it.overallStatus == HealthStatus.HEALTHY }

    suspend fun isReady(): Boolean =
        collectSummaries().all { it.overallStatus == HealthStatus.HEALTHY }

    suspend fun runAllCheckHealth(): Map<String, Any> {
        val startTime = System.currentTimeMillis()
        val summaries = collectSummaries()
        val systemStatus = HealthStatus.aggregate(summaries.map { it.overallStatus })

        return mapOf(
            "status" to systemStatus,
            "healthy" to (systemStatus != HealthStatus.UNHEALTHY),
            "ready" to (systemStatus == HealthStatus.HEALTHY),
            "timestamp" to Instant.now().toString(),
            "total_execution_time" to System.currentTimeMillis() - startTime,
            "modules" to summaries.associate { summary ->
                summary.moduleName to mapOf(
                    "status" to summary.overallStatus,
                    "duration_ms" to summary.durationMs,
                    "details" to summary.checks.map { check ->
                        mapOf(
                            "check_name" to check.name,
                            "status" to check.status,
                            "description" to check.description,
                            "metrics" to check.metrics,
                            "error" to check.error?.localizedMessage
                        )
                    }
                )
            }
        )
    }

    private suspend fun collectSummaries(): List<ModuleHealthSummary> = coroutineScope {
        checkers.map { checker ->
            async { runSafely(checker) }
        }.awaitAll()
    }

    private suspend fun runSafely(checker: IHealthChecker): ModuleHealthSummary {
        val startTime = System.currentTimeMillis()

        return try {
            checker.runCheckHealth()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ModuleHealthSummary(
                moduleName = checker.moduleName,
                overallStatus = HealthStatus.UNHEALTHY,
                checks = listOf(
                    HealthReport(
                        name = "Checker execution",
                        status = HealthStatus.UNHEALTHY,
                        description = "Health checker failed: ${e.localizedMessage}",
                        error = e
                    )
                ),
                durationMs = System.currentTimeMillis() - startTime
            )
        }
    }
}
