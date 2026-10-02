package adapters.repositories.query_extend

import adapters.repositories.IQueryExtend
import domain.entities.Goal
import domain.enums.GoalEvaluationType
import domain.enums.GoalStatusType
import java.util.UUID

class QueryGoalExtend(
    val sourceIds: Set<UUID>? = null,
    val status: domain.enums.GoalStatusType? = null,
    val type: domain.enums.GoalEvaluationType? = null,
    val dueDateComparator: QueryDateComparator? = null
): IQueryExtend<domain.entities.Goal> {
    override fun isStatisfy(entity: domain.entities.Goal): Boolean {
        if (!sourceIds.isNullOrEmpty() && !sourceIds.contains(entity.targetSourceId))
            return false

        if (status != null && status != entity.status)
            return false

        if (type != null && type != entity.type)
            return false


        if (dueDateComparator != null) {
            val resComp = when(dueDateComparator.comparator) {
                QueryComparator.Greater ->  dueDateComparator.date > entity.dueDate.atStartOfDay()
                QueryComparator.GreaterOrEquals -> dueDateComparator.date >= entity.dueDate.atStartOfDay()
                QueryComparator.Lesser -> dueDateComparator.date < entity.dueDate.atStartOfDay()
                QueryComparator.LesserOrEquals ->  dueDateComparator.date <= entity.dueDate.atStartOfDay()
                QueryComparator.Equal ->  dueDateComparator.date == entity.dueDate.atStartOfDay()
                else -> {false}
            }

            if (!resComp)
                return false
        }

        return true
    }
}