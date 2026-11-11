package utils.healths

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class RunnerHealthChecker(
    private val environment: Environment,
    @Value("\${agni.health.runner.required-config:}") private val requiredConfig: String = "",
) : IHealthChecker {

    override val moduleName: String = "Runner Infra health"

    override suspend fun runCheckHealth(): ModuleHealthSummary {
        val startTime = System.currentTimeMillis()

        val reports = requiredConfigKeys.map { key ->
            val value = environment.getProperty(key)
            val configured = !value.isNullOrBlank() && !value.startsWith("\${") && value != "null"

            HealthReport(
                name = key,
                status = if (configured) HealthStatus.HEALTHY else HealthStatus.UNHEALTHY,
                description = if (configured) {
                    "Infrastructure configuration '$key' is set."
                } else {
                    "Infrastructure configuration '$key' is missing."
                }
            )
        }

        return ModuleHealthSummary(
            moduleName = moduleName,
            overallStatus = HealthStatus.aggregate(reports.map { it.status }),
            checks = reports,
            durationMs = System.currentTimeMillis() - startTime
        )
    }

    private val requiredConfigKeys: List<String>
        get() = requiredConfig
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { DEFAULT_REQUIRED_CONFIG }

    companion object {
        private val DEFAULT_REQUIRED_CONFIG = listOf(
            "spring.datasource.url",
            "spring.datasource.username",
            "spring.datasource.password",
            "spring.data.redis.host",
            "spring.data.redis.port",
            "vectordb.qdrant.host",
            "vectordb.qdrant.port"
        )
    }
}
