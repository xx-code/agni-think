package usecases.categories

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.repositories.IRepository
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.entities.Category
import domain.entities.Color
import domain.entities.User
import domain.exceptions.AlreadyExistException
import domain.exceptions.ValidationException
import usecases.dto.CreatedOutput
import usecases.categories.dto.CreateCategoryInput
class CreateCategory(
    private val categoryRepo: IRepository<Category>,
    auth: IAuthenticateAdapter): UseCase<CreateCategoryInput, CreatedOutput>(auth = auth) {

    override suspend fun process(input: CreateCategoryInput): CreatedOutput {
        getCurrentUser()

        val condition = QueryExtendBuilder<Category>()
            .addCondition("title", QueryComparator.Equal, input.title)
        if (categoryRepo.exist(condition))
            throw AlreadyExistException.EntitiesByField(mapOf("name" to "Category title already exists"), "category")

        if (input.isSystem == true)
            throw ValidationException.UserCannotCreateSystemTag()

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