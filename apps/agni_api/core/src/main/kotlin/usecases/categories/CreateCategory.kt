package usecases.categories

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Category
import domain.entities.Color
import domain.exceptions.AlreadyExistException
import usecases.dto.CreatedOutput
import usecases.categories.dto.CreateCategoryInput
class CreateCategory(private val categoryRepo: IRepository<Category>): UseCase<CreateCategoryInput, CreatedOutput>() {

    override suspend fun process(input: CreateCategoryInput): CreatedOutput {
        if (categoryRepo.existsByName(input.title))
            throw AlreadyExistException.EntitiesByField(mapOf("name" to "Category title already exists"), "category")

        val newCategory = Category(
            title = input.title,
            color = Color(input.color),
            icon = input.icon,
            isSystem =  input.isSystem?: false
        )

        categoryRepo.create(newCategory)

        return CreatedOutput(newCategory.id)
    }
}