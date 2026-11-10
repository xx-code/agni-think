package utils.healths

import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit

@Component
class QdrantHealthChecker(
    @Value("\${vectordb.qdrant.host}") private val host: String,
    @Value("\${vectordb.qdrant.port:6334}") private val port: Int,
    @Value("\${vectordb.qdrant.timeout-ms:5000}") private val timeoutMs: Long = 5000,
) : IHealthChecker {

    override val moduleName: String = "Qdrant vector database"

    private val client: QdrantClient =
        QdrantClient(QdrantGrpcClient.newBuilder(host, port, false).build())

    override suspend fun runCheckHealth(): ModuleHealthSummary = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val reports = mutableListOf<HealthReport>()
        val endpoint = "$host:$port"

        try {
            val requestStart = System.currentTimeMillis()
            val health = client.healthCheckAsync(Duration.ofMillis(timeoutMs))
                .get(timeoutMs, TimeUnit.MILLISECONDS)
            reports += HealthReport(
                name = "gRPC Health",
                status = HealthStatus.HEALTHY,
                description = "Qdrant ${health.title} ${health.version} is reachable.",
                metrics = mapOf(
                    "endpoint" to endpoint,
                    "title" to health.title,
                    "version" to health.version,
                    "commit" to health.commit,
                    "latency_ms" to System.currentTimeMillis() - requestStart
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            reports += HealthReport(
                name = "gRPC Health",
                status = HealthStatus.UNHEALTHY,
                description = "Unable to reach Qdrant at $endpoint: ${e.localizedMessage}",
                error = e,
                metrics = mapOf("endpoint" to endpoint)
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
