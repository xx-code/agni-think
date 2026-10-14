package usecases.tags

import adapters.IAuthenticateAdapter
import adapters.readers.IGetAllTagReader
import usecases.UseCase
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Tag
import usecases.dto.ListOutput
import usecases.tags.dto.GetAllTagInput
import usecases.tags.dto.GetTagOutput

class GetAllTags(private val tagRepo: IGetAllTagReader, auth: IAuthenticateAdapter): UseCase<GetAllTagInput, ListOutput<GetTagOutput>>(auth = auth) {
    override suspend fun process(input: GetAllTagInput): ListOutput<GetTagOutput> {
        val currentUser = getCurrentUser()
        val condition = QueryExtendBuilder<Tag>()

        if (input.isSystem != null)
            condition.addCondition("isSystem", QueryComparator.Equal, input.isSystem)

        if (input.isArchived != null)
            condition.addCondition("isArchived", QueryComparator.Equal, input.isArchived)

        val tags = tagRepo.read(input.query, currentUser.id, condition)

        return ListOutput(
            items = tags.items.map {
                GetTagOutput(
                    id = it.id,
                    value = it.value + if (it.isArchived) " (Archiver)" else "",
                    color = it.color.toString(),
                    isSystem = it.isSystem,
                    isArchived = it.isArchived
                )
            },
            total = tags.total
        )
    }
}