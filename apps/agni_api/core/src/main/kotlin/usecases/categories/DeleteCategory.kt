package usecases.categories

import usecases.UseCase
import adapters.IChecker
import adapters.repositories.IRepository
import domain.entities.Category
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import usecases.categories.dto.DeleteCategoryInput
import usecases.dto.DeleteOutput
class DeleteCategory(
    private val categoryRepo: IRepository<Category>,
    private val categoryChecker: IChecker<Category>
): UseCase<DeleteCategoryInput, DeleteOutput>() {
    override suspend fun process(input: DeleteCategoryInput): DeleteOutput {
        val category = categoryRepo.get(input.categoryId) ?: throw NotFoundException.SingleEntity(input.categoryId, "category")
        if (category.isSystem)
            throw ValidationException.CantDeleteSystemCategory(category.title)

        if (categoryChecker.isInUse(category))
            return DeleteOutput.inUse()

        categoryRepo.delete(input.categoryId)

        return DeleteOutput.success()
    }
}