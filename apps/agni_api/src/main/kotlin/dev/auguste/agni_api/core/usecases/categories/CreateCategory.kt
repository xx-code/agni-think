package dev.auguste.agni_api.core.usecases.categories

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.categories.dto.CreateCategoryInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class CreateCategory(val categoryRepo: IRepository<Category>): IUseCase<CreateCategoryInput, CreatedOutput> {

    override fun execAsync(input: CreateCategoryInput): CreatedOutput {
        if (categoryRepo.existsByName(input.title))
            throw Error("Category title already exists")

        val newCategory = Category(
            title = input.title,
            color = input.color,
            icon = input.icon,
            isSystem =  input.isSystem?: false
        )

        categoryRepo.create(newCategory)

        return CreatedOutput(newCategory.id)
    }
}