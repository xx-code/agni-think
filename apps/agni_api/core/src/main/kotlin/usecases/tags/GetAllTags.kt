package usecases.tags

import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.query_extend.QueryComparator
import adapters.repositories.query_extend.QueryTagExtend
import domain.entities.Category
import domain.entities.Tag
import usecases.ListOutput
import usecases.interfaces.IUseCase
import usecases.tags.dto.GetAllTagInput
import usecases.tags.dto.GetTagOutput

class GetAllTags(private val tagRepo: IRepository<Tag>): IUseCase<GetAllTagInput, ListOutput<GetTagOutput>> {
    override fun execAsync(input: GetAllTagInput): ListOutput<GetTagOutput> {
        val condition = QueryExtendBuilder<Tag>()

        if (input.isSystem != null)
            condition.addCondition("isSystem", QueryComparator.Equal, input.isSystem)

        if (input.isArchived != null)
            condition.addCondition("isArchived", QueryComparator.Equal, input.isArchived)

        val tags = tagRepo.getAll(input.query, condition)

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