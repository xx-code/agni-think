package dev.auguste.agni_api.core.adapters.repositories.query_extend

import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.entities.enums.PeriodType

class QuerySpendingPeriodTemplateExtend(
    val period: PeriodType? = null,
    val interval: Int? = null,
    val isActive: Boolean? = null
) : IQueryExtend<SpendingPeriodTemplate> {
    override fun isStatisfy(entity: SpendingPeriodTemplate): Boolean {
        if (period != null && entity.recurrence.period != period)
            return false

        if (interval != null && entity.recurrence.interval != interval)
            return false

        if (isActive == null && !entity.checkIsActive())
            return false

        return true
    }
}