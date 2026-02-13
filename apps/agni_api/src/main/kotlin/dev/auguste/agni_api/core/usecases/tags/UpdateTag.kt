package dev.auguste.agni_api.core.usecases.tags

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.tags.dto.UpdateTagInput

class UpdateTag(private val tagRepo: IRepository<Tag>): IUseCase<UpdateTagInput, Unit> {

    override fun execAsync(input: UpdateTagInput) {
        val tag = tagRepo.get(input.id) ?: throw Error("Tag with id $input.id not found.")

        if (input.value != null)
            tag.value = input.value

        if (input.color != null)
            tag.color = input.color

        if (tag.hasChanged())
            tagRepo.update(tag)
    }

}