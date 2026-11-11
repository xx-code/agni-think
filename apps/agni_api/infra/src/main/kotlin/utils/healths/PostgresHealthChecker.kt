package utils.healths

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.sql.SQLException
import javax.sql.DataSource

@Component
class PostgresHealthChecker(
    private val dataSource: DataSource
) : IHealthChecker {

    override val moduleName: String = "Postgres Database"

    override suspend fun runCheckHealth(): ModuleHealthSummary = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val reports = mutableListOf<HealthReport>()

        try {
            val latency = checkConnection()
            reports += HealthReport(
                name = "Connection",
                status = HealthStatus.HEALTHY,
                description = "Connection to Postgres succeeded.",
                metrics = mapOf("latency_ms" to latency)
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            reports += HealthReport(
                name = "Connection",
                status = HealthStatus.UNHEALTHY,
                description = "Unable to connect to Postgres: ${e.localizedMessage}",
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

    private fun checkConnection(): Long {
        val startTime = System.currentTimeMillis()

        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT 1").use { resultSet ->
                    if (!resultSet.next() || resultSet.getInt(1) != 1) {
                        throw SQLException("Unexpected result while executing SELECT 1")
                    }
                }
            }
        }

        return System.currentTimeMillis() - startTime
    }
}
