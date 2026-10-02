package usecases.categories

import adapters.repositories.IRepository
import domain.entities.Category
import domain.entities.Color
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import usecases.categories.dto.UpdateCategoryInput
import usecases.interfaces.IUseCase

class UpdateCategory(private val categoryRepo: IRepository<Category>): IUseCase<UpdateCategoryInput, Unit> {

    override fun execAsync(input: UpdateCategoryInput) {
        val category = categoryRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "category")

        if (input.title != null) {
            if (input.title != category.title && categoryRepo.existsByName(input.title))
                throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "category")

            category.title = input.title
        }

        if (input.icon != null)
            category.icon = input.icon

        if (input.color != null)
            category.color = Color(input.color)

        if (input.isArchived != null)
            category.isArchived = input.isArchived

        if (category.hasChanged())
            categoryRepo.update(category)
    }
}