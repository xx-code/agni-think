package usecases.categories

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Category
import domain.entities.Color
import domain.exceptions.NotFoundException
import usecases.categories.dto.GetCategoryOutput
import java.util.UUID

class GetCategory(private val categoryRepo: IRepository<Category>, auth: IAuthenticateAdapter): UseCase<UUID, GetCategoryOutput>(auth = auth) {

    override suspend fun process(input: UUID): GetCategoryOutput {
        val category = categoryRepo.get(input)?: throw NotFoundException.SingleEntity(input, "category")

        return GetCategoryOutput(
            id = category.id,
            title = category.title + if (category.isArchived) " (Archiver)" else "",
            color = category.color.toString(),
            icon = category.icon,
            isSystem = category.isSystem,
            isArchive = category.isArchived
        )
    }
}