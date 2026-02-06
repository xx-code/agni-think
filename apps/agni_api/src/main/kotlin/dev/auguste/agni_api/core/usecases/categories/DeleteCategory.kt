package dev.auguste.agni_api.core.usecases.categories

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class DeleteCategory(val categoryRepo: IRepository<Category>): IUseCase<UUID, Unit> {

    override fun execAsync(input: UUID) {
        categoryRepo.get(input)?: throw Error("Category $input not found")

        categoryRepo.delete(input)
    }
}