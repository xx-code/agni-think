package dev.auguste.agni_api.core.adapters.repositories.query_extend

import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.Goal
import dev.auguste.agni_api.core.entities.enums.GoalEvaluationType
import dev.auguste.agni_api.core.entities.enums.GoalStatusType
import java.util.UUID

class QueryGoalExtend(
    val sourceIds: Set<UUID>? = null,
    val status: GoalStatusType? = null,
    val type: GoalEvaluationType? = null,
    val dueDateComparator: QueryDateComparator? = null
): IQueryExtend<Goal> {
    override fun isStatisfy(entity: Goal): Boolean {
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