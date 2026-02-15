package dev.auguste.agni_api.core.adapters.repositories.query_extend

import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.entities.Tag

class QueryTagExtend(
    val isSystem: Boolean?,
) : IQueryExtend<Tag> {
    override fun isStatisfy(entity: Tag): Boolean {
        if (isSystem != null)
            return isSystem == entity.isSystem

        return true
    }

}