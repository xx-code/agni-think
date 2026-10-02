package adapters.repositories.query_extend

import adapters.repositories.IQueryExtend
import java.util.UUID

class QuerySavingGoalExtend(
    val accountIds: Set<UUID>
): IQueryExtend<domain.entities.Fund> {
    override fun isStatisfy(entity: domain.entities.Fund): Boolean {
        return accountIds.contains(entity.accountId)
    }
}