package dev.auguste.agni_api.core.usecases.categories

import dev.auguste.agni_api.core.adapters.IChecker
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.usecases.categories.dto.DeleteCategoryInput
import dev.auguste.agni_api.core.usecases.DeleteOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class DeleteCategory(
    private val categoryRepo: IRepository<Category>,
    private val categoryChecker: IChecker<Category>
): IUseCase<DeleteCategoryInput, DeleteOutput> {
    override fun execAsync(input: DeleteCategoryInput): DeleteOutput {
        val category = categoryRepo.get(input.categoryId) ?: throw DomainException.NotFound.Category(input.categoryId)
        if (category.isSystem)
            throw DomainException.BusinessLogic.CantDeleteSystemCategory(category.title)

        if (categoryChecker.isInUse(category))
            return DeleteOutput.inUse()

        categoryRepo.delete(input.categoryId)

        return DeleteOutput.success()
    }
}