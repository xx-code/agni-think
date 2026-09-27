package dev.auguste.agni_api.core.value_objects

data class SnapshotForcastSpendingPeriod(
    val income: Double,
    val fixExpenses: Double,
    val variableExpenses: Double,
    val budgetExpenses: Double,
    val saving: Double
): IValueObject {
    override fun toMap(): Map<String, Any> {
        return mapOf(
            "income" to income,
            "fixExpenses" to fixExpenses,
            "variableExpenses" to variableExpenses,
            "budgetExpenses" to budgetExpenses,
            "saving" to saving
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): SnapshotForcastSpendingPeriod {
            val empty = SnapshotForcastSpendingPeriod(0.0, 0.0, 0.0, 0.0, 0.0)
            if (map == null)
                return empty

            if (!map.containsKey("income") || !map.containsKey("fixExpenses")
                || !map.containsKey("variableExpenses") || !map.containsKey("budgetExpenses")
                || !map.containsKey("saving"))
                return empty

            return SnapshotForcastSpendingPeriod(
                income = toDouble(map["income"]),
                fixExpenses = toDouble(map["fixExpenses"]),
                variableExpenses = toDouble(map["variableExpenses"]),
                budgetExpenses = toDouble(map["budgetExpenses"]),
                saving = toDouble(map["saving"])
            )
        }

        private fun toDouble(value: Any?): Double {
            return when (value) {
                is Double -> value
                is Number -> value.toDouble()
                else -> 0.0
            }
        }
    }
}
