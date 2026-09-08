package dev.auguste.agni_api.core.adapters.repositories.query_extend

import java.time.LocalDateTime

enum class QueryComparator {
    Greater,
    GreaterOrEquals,
    Lesser,
    LesserOrEquals,
    Equal,
    In
}

data class QueryDateComparator(
    val date: LocalDateTime,
    val endDate: LocalDateTime? = null,
    val comparator: QueryComparator
) {
    fun isSatisfyComparison(compareDate: LocalDateTime): Boolean {
        return when(comparator) {
            QueryComparator.Greater ->  date > compareDate
            QueryComparator.GreaterOrEquals -> date >= compareDate
            QueryComparator.Lesser -> date < compareDate
            QueryComparator.LesserOrEquals ->  date <= compareDate
            QueryComparator.Equal ->  date == compareDate
            QueryComparator.In ->  {
                if (endDate == null)
                    false
                else
                    date in date..endDate
            }
        }
    }
}
