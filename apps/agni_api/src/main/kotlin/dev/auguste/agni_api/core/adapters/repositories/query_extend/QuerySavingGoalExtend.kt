package dev.auguste.agni_api.core.adapters.repositories.query_extend

import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.SavingGoal
import java.util.UUID

class QuerySavingGoalExtend(
    val accountIds: Set<UUID>
): IQueryExtend<SavingGoal> {
    override fun isStatisfy(entity: SavingGoal): Boolean {
        return accountIds.contains(entity.accountId)
    }
}