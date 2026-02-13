package dev.auguste.agni_api.core.adapters.repositories.query_extend

import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.Budget

class QueryBudgetExtend(
    val scheduleDueDateComparator: QueryDateComparator
): IQueryExtend<Budget> {
    override fun isStatisfy(entity: Budget): Boolean {
        return when(scheduleDueDateComparator.comparator) {
            ComparatorType.Greater ->  scheduleDueDateComparator.date > entity.scheduler.date
            ComparatorType.GreaterOrEquals -> scheduleDueDateComparator.date >= entity.scheduler.date
            ComparatorType.Lesser -> scheduleDueDateComparator.date < entity.scheduler.date
            ComparatorType.LesserOrEquals ->  scheduleDueDateComparator.date <= entity.scheduler.date
            ComparatorType.Equal ->  scheduleDueDateComparator.date == entity.scheduler.date
        }
    }
}