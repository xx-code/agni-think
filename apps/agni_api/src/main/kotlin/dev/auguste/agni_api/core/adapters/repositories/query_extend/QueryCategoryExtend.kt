package dev.auguste.agni_api.core.adapters.repositories.query_extend

import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.Category

class QueryCategoryExtend(
    val isSystem: Boolean?,
) : IQueryExtend<Category> {
    override fun isStatisfy(entity: Category): Boolean {
        if (isSystem != null)
            return isSystem == entity.isSystem

        return true
    }

}