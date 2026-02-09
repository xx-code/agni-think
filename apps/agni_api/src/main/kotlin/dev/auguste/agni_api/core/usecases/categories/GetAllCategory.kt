package dev.auguste.agni_api.core.usecases.categories

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.categories.dto.GetCategoryOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class GetAllCategory(private val categoryRepo: IRepository<Category>): IUseCase<QueryFilter, ListOutput<GetCategoryOutput>> {

    override fun execAsync(input: QueryFilter): ListOutput<GetCategoryOutput> {
        val categories = categoryRepo.getAll(input)

        return ListOutput(
            items = categories.items.map { GetCategoryOutput(
                id = it.id,
                title = it.title,
                color = it.color,
                icon = it.icon,
                isSystem = it.isSystem
            ) },
            total = categories.total
        )
    }

}