package dev.auguste.agni_api.core.adapters.repositories.query_extend

import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.SpendingPeriod
import dev.auguste.agni_api.core.entities.enums.SpendingPeriodStateType
import java.time.LocalDate

class QuerySpendingPeriodExtend(
    val state: SpendingPeriodStateType? = null,
    val compartorStartDate: QueryDateComparator? = null,
    val comparatorEndDate: QueryDateComparator? = null
): IQueryExtend<SpendingPeriod>{
    override fun isStatisfy(entity: SpendingPeriod): Boolean {
        if (state != null && entity.state != state)
            return false

        if (compartorStartDate != null && !compartorStartDate.isSatisfyComparison(entity.startDate.atStartOfDay()))
            return false

        if (comparatorEndDate != null && !comparatorEndDate.isSatisfyComparison(entity.endDate.atStartOfDay()))
            return false

        return true
    }
}