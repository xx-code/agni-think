package dev.auguste.agni_api.core.usecases.categories

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.usecases.categories.dto.UpdateCategoryInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class UpdateCategory(private val categoryRepo: IRepository<Category>): IUseCase<UpdateCategoryInput, Unit> {

    override fun execAsync(input: UpdateCategoryInput) {
        val category = categoryRepo.get(input.id) ?: throw Error("Category ${input.id} not found")

        if (input.title != null) {
            if (input.title != category.title && categoryRepo.existsByName(input.title))
                throw Error("Category ${input.title} already exists")

            category.title = input.title
        }

        if (input.icon != null)
            category.icon = input.icon

        if (input.color != null)
            category.color = input.color

        if (category.hasChanged())
            categoryRepo.update(category)
    }
}