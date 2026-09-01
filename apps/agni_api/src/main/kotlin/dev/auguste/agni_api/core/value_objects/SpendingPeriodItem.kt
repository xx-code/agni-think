package dev.auguste.agni_api.core.value_objects

data class SpendingPeriodItem(val description: String, val amount: Double): IValueObject {
    override fun toMap(): Map<String, Any> {
        return mapOf( "description" to description, "amount" to amount)
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): SpendingPeriodItem {
            val empty = SpendingPeriodItem("", 0.0)
            if (map == null)
                return empty

            if (!map.containsKey("description") && !map.containsKey("amount"))
                return empty

            var amount = map["amount"]
            if (amount is Int)
                amount = amount.toDouble()

            return SpendingPeriodItem(description = map["description"] as String, amount = amount as Double,)
        }
    }
}
