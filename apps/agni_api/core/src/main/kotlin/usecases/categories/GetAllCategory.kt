package usecases.categories

import adapters.dto.QueryFilter
import adapters.dto.QuerySortBy
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Category
import usecases.ListOutput
import usecases.categories.dto.GetAllCategoryInput
import usecases.categories.dto.GetCategoryOutput
import usecases.interfaces.IUseCase

class GetAllCategory(private val categoryRepo: IRepository<Category>): IUseCase<GetAllCategoryInput, ListOutput<GetCategoryOutput>> {

    override fun execAsync(input: GetAllCategoryInput): ListOutput<GetCategoryOutput> {
        val condition = QueryExtendBuilder<Category>()

        if (input.isSystem != null)
            condition.addCondition("isSystem", QueryComparator.Equal, input.isSystem)

        if (input.isArchived != null)
            condition.addCondition("isArchived", QueryComparator.Equal, input.isArchived)

        val categories = categoryRepo.getAll(
            query = input.query,
            condition
        )

        return ListOutput(
            items = categories.items.map { GetCategoryOutput(
                id = it.id,
                title = it.title + if (it.isArchived) " (Archiver)" else "",
                color = it.color.toString(),
                icon = it.icon,
                isSystem = it.isSystem,
                isArchive = it.isArchived
            ) },
            total = categories.total
        )
    }

}