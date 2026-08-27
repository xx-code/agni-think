package dev.auguste.agni_api.core.entities.enums

enum class DepreciationType(val value: String) {
    DECLINING_BALANCE("DecliningBalance"),
    STRAIGHT_LINE("StraightLine"),
    FIX("fix"),
    FIX_PERCENTAGE("FixPercentage");

    companion object {
        fun fromString(value: String): DepreciationType {
            return DepreciationType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Depreciation Type $value not found in enums")
        }
    }
}