package adapters.repositories.query_extend

import adapters.repositories.IQueryExtend
import domain.entities.Category

class QueryCategoryExtend(
    val isSystem: Boolean?,
) : IQueryExtend<domain.entities.Category> {
    override fun isStatisfy(entity: domain.entities.Category): Boolean {
        if (isSystem != null)
            return isSystem == entity.isSystem

        return true
    }

}