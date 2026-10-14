package usecases.categories

import adapters.IAuthenticateAdapter
import adapters.readers.IGetAllCategoryReader
import usecases.UseCase
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Category
import usecases.dto.ListOutput
import usecases.categories.dto.GetAllCategoryInput
import usecases.categories.dto.GetCategoryOutput
class GetAllCategory(private val categoryRepo: IGetAllCategoryReader, auth: IAuthenticateAdapter): UseCase<GetAllCategoryInput, ListOutput<GetCategoryOutput>>(auth = auth) {

    override suspend fun process(input: GetAllCategoryInput): ListOutput<GetCategoryOutput> {
        val currentUser = getCurrentUser()
        val condition = QueryExtendBuilder<Category>()

        if (input.isSystem != null)
            condition.addCondition("isSystem", QueryComparator.Equal, input.isSystem)

        if (input.isArchived != null)
            condition.addCondition("isArchived", QueryComparator.Equal, input.isArchived)

        val categories = categoryRepo.read(
            query = input.query,
            currentUser.id,
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