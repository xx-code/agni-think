package usecases.categories

import adapters.repositories.IRepository
import domain.entities.Category
import domain.entities.Color
import domain.exceptions.NotFoundException
import usecases.categories.dto.GetCategoryOutput
import usecases.interfaces.IUseCase
import java.util.UUID

class GetCategory(private val categoryRepo: IRepository<Category>): IUseCase<UUID, GetCategoryOutput> {

    override fun execAsync(input: UUID): GetCategoryOutput {
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