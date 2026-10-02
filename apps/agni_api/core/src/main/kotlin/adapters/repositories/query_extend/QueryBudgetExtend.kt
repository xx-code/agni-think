package adapters.repositories.query_extend

import adapters.repositories.IQueryExtend
import domain.entities.Budget
import domain.enums.PeriodType

class QueryBudgetExtend(
    val scheduleDueDateComparator: QueryDateComparator? = null,
    val periodTypes: Set<domain.enums.PeriodType>? = null
): IQueryExtend<domain.entities.Budget> {
    override fun isStatisfy(entity: domain.entities.Budget): Boolean {
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