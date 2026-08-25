package dev.auguste.agni_api.core.value_objects

import dev.auguste.agni_api.core.entities.enums.DepreciationType

data class ProvisionDepreciateCriteria(
    val title: String,
    val description: String,
    val type: DepreciationType,
    val value: Double,
    val monthRange: Int = 0
): IValueObject {
    override fun toMap(): Map<String, Any> {
        return mapOf(
            "title" to title,
            "description" to description,
            "type" to type.value,
            "value" to value,
            "monthRange" to monthRange
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): ProvisionDepreciateCriteria {
            if (map == null)
                return ProvisionDepreciateCriteria("", "", DepreciationType.FIX, 0.0)

            if (!map.containsKey("title") || !map.containsKey("description") || !map.containsKey("type")
                || !map.containsKey("value") || !map.containsKey("monthRange"))
                return ProvisionDepreciateCriteria("", "", DepreciationType.FIX, 0.0)

            var value = map["value"]
            if (value is Int)
                value = value.toDouble()

            return ProvisionDepreciateCriteria(
                title = map["title"] as String,
                description = map["description"] as String,
                type = DepreciationType.fromString(map["type"] as String),
                value = value as Double,
                monthRange = map["monthRange"] as Int
            )
        }
    }
}
