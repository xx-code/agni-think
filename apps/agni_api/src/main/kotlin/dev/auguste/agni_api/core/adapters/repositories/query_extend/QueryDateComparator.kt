package dev.auguste.agni_api.core.adapters.repositories.query_extend

import java.time.LocalDateTime

enum class ComparatorType {
    Greater,
    GreaterOrEquals,
    Lesser,
    LesserOrEquals,
    Equal
}

data class QueryDateComparator(
    val date: LocalDateTime,
    val comparator: ComparatorType
)
