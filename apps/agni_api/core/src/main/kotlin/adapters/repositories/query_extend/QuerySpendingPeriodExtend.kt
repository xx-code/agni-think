package adapters.repositories.query_extend

import adapters.repositories.IQueryExtend
import domain.entities.SpendingPeriod
import domain.enums.SpendingPeriodStateType

class QuerySpendingPeriodExtend(
    val state: domain.enums.SpendingPeriodStateType? = null,
    val compartorStartDate: QueryDateComparator? = null,
    val comparatorEndDate: QueryDateComparator? = null
): IQueryExtend<domain.entities.SpendingPeriod>{
    override fun isStatisfy(entity: domain.entities.SpendingPeriod): Boolean {
        if (state != null && entity.state != state)
            return false

        if (compartorStartDate != null && !compartorStartDate.isSatisfyComparison(entity.startDate.atStartOfDay()))
            return false

        if (comparatorEndDate != null && !comparatorEndDate.isSatisfyComparison(entity.endDate.atStartOfDay()))
            return false

        return true
    }
}