package dev.auguste.agni_api.core.usecases.tags

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.tags.dto.GetTagOutput
import java.awt.print.Pageable

class GetAllTags(private val tagRepo: IRepository<Tag>): IUseCase<QueryFilter, ListOutput<GetTagOutput>> {
    override fun execAsync(input: QueryFilter): ListOutput<GetTagOutput> {
        val tags = tagRepo.getAll(input)

        return ListOutput(
            items = tags.items.map {
                GetTagOutput(
                    id = it.id,
                    value = it.value,
                    color = it.color,
                    isSystem = it.isSystem
                )
            },
            total = tags.total
        )
    }
}