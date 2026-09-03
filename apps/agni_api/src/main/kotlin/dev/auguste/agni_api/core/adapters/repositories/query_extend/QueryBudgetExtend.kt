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
                QueryComparator.Greater ->  scheduleDueDateComparator.date > entity.scheduler.date
                QueryComparator.GreaterOrEquals -> scheduleDueDateComparator.date >= entity.scheduler.date
                QueryComparator.Lesser -> scheduleDueDateComparator.date < entity.scheduler.date
                QueryComparator.LesserOrEquals ->  scheduleDueDateComparator.date <= entity.scheduler.date
                QueryComparator.Equal ->  scheduleDueDateComparator.date == entity.scheduler.date
                else -> {false}
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