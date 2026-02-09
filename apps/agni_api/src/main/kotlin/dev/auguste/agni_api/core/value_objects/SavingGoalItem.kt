package dev.auguste.agni_api.core.value_objects

data class SavingGoalItem(val title: String, val url: String, val price: Double) {
    fun toMap(): Map<String, Any> {
        return mapOf("title" to title, "url" to url, "price" to price)
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): SavingGoalItem {
            if (map == null)
                return SavingGoalItem("", "", 0.0)

            if (!map.containsKey("title") || !map.containsKey("url") || !map.containsKey("price"))
                return SavingGoalItem("", "", 0.0)


            var price = map["price"]
            if (price is Int)
                price = price.toDouble()

            return SavingGoalItem(
                title = map.getValue("title") as String,
                url = map.getValue("url") as String,
                price = price as Double
            )
        }
    }
}
