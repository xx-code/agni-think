package dev.auguste.agni_api.core.usecases.categories

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.usecases.categories.dto.GetCategoryOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class GetCategory(private val categoryRepo: IRepository<Category>): IUseCase<UUID, GetCategoryOutput> {

    override fun execAsync(input: UUID): GetCategoryOutput {
        val category = categoryRepo.get(input)?: throw Error("Category $input not found")

        return GetCategoryOutput(
            id = category.id,
            title = category.title,
            color = category.color,
            icon = category.icon,
            isSystem = category.isSystem,
            )
    }
}