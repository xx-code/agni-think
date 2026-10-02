package adapters.repositories.query_extend

import adapters.repositories.IQueryExtend
import domain.entities.SpendingPeriodTemplate
import domain.enums.PeriodType

class QuerySpendingPeriodTemplateExtend(
    val period: domain.enums.PeriodType? = null,
    val interval: Int? = null,
    val isActive: Boolean? = null
) : IQueryExtend<domain.entities.SpendingPeriodTemplate> {
    override fun isStatisfy(entity: domain.entities.SpendingPeriodTemplate): Boolean {
        if (period != null && entity.recurrence.period != period)
            return false

        if (interval != null && entity.recurrence.interval != interval)
            return false

        if (isActive == null && !entity.checkIsActive())
            return false

        return true
    }
}