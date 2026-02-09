package dev.auguste.agni_api.core.usecases.categories

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.usecases.categories.dto.DeleteCategoryInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class DeleteCategory(private val categoryRepo: IRepository<Category>): IUseCase<DeleteCategoryInput, Unit> {

    override fun execAsync(input: DeleteCategoryInput) {
        categoryRepo.get(input.categoryId)?: throw Error("Category $input not found")

        categoryRepo.delete(input.categoryId)
    }
}