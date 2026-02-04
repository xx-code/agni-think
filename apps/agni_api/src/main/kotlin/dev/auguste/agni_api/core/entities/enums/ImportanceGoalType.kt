package dev.auguste.agni_api.core.entities.enums

enum class ImportanceGoalType(val type: Int) {
    INSIGNIFICANT(1),
    NORMAL(2),
    IMPORTANT(3),
    URGENT(4)
}