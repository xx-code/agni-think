package dev.auguste.agni_api.core.usecases.categories

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.dto.QuerySortBy
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.QueryExtendBuilder
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryCategoryExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.categories.dto.GetAllCategoryInput
import dev.auguste.agni_api.core.usecases.categories.dto.GetCategoryOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class GetAllCategory(private val categoryRepo: IRepository<Category>): IUseCase<GetAllCategoryInput, ListOutput<GetCategoryOutput>> {

    override fun execAsync(input: GetAllCategoryInput): ListOutput<GetCategoryOutput> {
        val condition = QueryExtendBuilder<Category>()

        if (input.isSystem != null)
            condition.addCondition("isSystem", QueryComparator.Equal, input.isSystem)

        if (input.isSystem != null)
            condition.addCondition("isArchived", QueryComparator.Equal, input.isArchived)

        val categories = categoryRepo.getAll(
            query = input.query,
            condition
        )

        return ListOutput(
            items = categories.items.map { GetCategoryOutput(
                id = it.id,
                title = it.title + if (it.isArchived) " (Archiver)" else "",
                color = it.color,
                icon = it.icon,
                isSystem = it.isSystem,
                isArchive = it.isArchived
            ) },
            total = categories.total
        )
    }

}