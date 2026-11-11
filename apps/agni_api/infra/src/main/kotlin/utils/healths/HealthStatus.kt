package utils.healths

enum class HealthStatus {
    HEALTHY,
    DEGRADED,
    UNHEALTHY;

    companion object {
        fun aggregate(statuses: Iterable<HealthStatus>): HealthStatus {
            val values = statuses.toList()
            return when {
                values.any { it == UNHEALTHY } -> UNHEALTHY
                values.any { it == DEGRADED } -> DEGRADED
                else -> HEALTHY
            }
        }
    }
}
