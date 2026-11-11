package utils.healths

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.redis.connection.RedisConnection
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisHealthChecker(
    private val redis: StringRedisTemplate
) : IHealthChecker {

    override val moduleName: String = "Redis cache"

    override suspend fun runCheckHealth(): ModuleHealthSummary = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val reports = mutableListOf<HealthReport>()

        try {
            val requestStart = System.currentTimeMillis()
            val pong = redis.execute { connection: RedisConnection -> connection.ping() }
            val latency = System.currentTimeMillis() - requestStart
            val healthy = pong?.equals("PONG", ignoreCase = true) == true

            reports += HealthReport(
                name = "PING",
                status = if (healthy) HealthStatus.HEALTHY else HealthStatus.UNHEALTHY,
                description = if (healthy) {
                    "Redis replied to PING."
                } else {
                    "Unexpected Redis PING response: $pong"
                },
                metrics = mapOf(
                    "response" to (pong ?: "null"),
                    "latency_ms" to latency
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            reports += HealthReport(
                name = "PING",
                status = HealthStatus.UNHEALTHY,
                description = "Unable to reach Redis: ${e.localizedMessage}",
                error = e
            )
        }

        ModuleHealthSummary(
            moduleName = moduleName,
            overallStatus = HealthStatus.aggregate(reports.map { it.status }),
            checks = reports,
            durationMs = System.currentTimeMillis() - startTime
        )
    }
}
