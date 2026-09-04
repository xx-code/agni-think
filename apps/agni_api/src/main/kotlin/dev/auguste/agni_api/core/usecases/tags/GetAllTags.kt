package dev.auguste.agni_api.core.usecases.tags

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.QueryExtendBuilder
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryTagExtend
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.tags.dto.GetAllTagInput
import dev.auguste.agni_api.core.usecases.tags.dto.GetTagOutput

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
                    color = it.color,
                    isSystem = it.isSystem,
                    isArchived = it.isArchived
                )
            },
            total = tags.total
        )
    }
}