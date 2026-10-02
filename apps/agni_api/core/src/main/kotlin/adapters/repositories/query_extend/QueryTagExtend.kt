package adapters.repositories.query_extend

import adapters.repositories.IQueryExtend
import domain.entities.Tag

class QueryTagExtend(
    val isSystem: Boolean?,
) : IQueryExtend<domain.entities.Tag> {
    override fun isStatisfy(entity: domain.entities.Tag): Boolean {
        if (isSystem != null)
            return isSystem == entity.isSystem

        return true
    }

}