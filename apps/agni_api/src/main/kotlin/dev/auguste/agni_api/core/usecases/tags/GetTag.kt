package dev.auguste.agni_api.core.usecases.tags

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.tags.dto.GetTagOutput
import java.util.UUID

class GetTag(private val tagRepo: IRepository<Tag>): IUseCase<UUID, GetTagOutput> {

    override fun execAsync(input: UUID): GetTagOutput {
        val tag = tagRepo.get(input) ?: throw Error("Tag with name $input not found.")

        return GetTagOutput(
            id = tag.id,
            value = tag.value,
            color = tag.color,
            isSystem = tag.isSystem
        )
    }
}