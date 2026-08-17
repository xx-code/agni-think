package dev.auguste.agni_api.core.adapters.repositories.query_extend

import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.enums.PeriodType

class QueryBudgetExtend(
    val scheduleDueDateComparator: QueryDateComparator? = null,
    val periodTypes: Set<PeriodType>? = null
): IQueryExtend<Budget> {
    override fun isStatisfy(entity: Budget): Boolean {
        if (scheduleDueDateComparator != null) {
            val resComp = when(scheduleDueDateComparator.comparator) {
                ComparatorType.Greater ->  scheduleDueDateComparator.date > entity.scheduler.date
                ComparatorType.GreaterOrEquals -> scheduleDueDateComparator.date >= entity.scheduler.date
                ComparatorType.Lesser -> scheduleDueDateComparator.date < entity.scheduler.date
                ComparatorType.LesserOrEquals ->  scheduleDueDateComparator.date <= entity.scheduler.date
                ComparatorType.Equal ->  scheduleDueDateComparator.date == entity.scheduler.date
            }

            if (!resComp)
                return false
        }

        if(entity.scheduler.repeater != null)
            if (periodTypes != null && !periodTypes.contains(entity.scheduler.repeater!!.period))
                return false

        return true
    }
}