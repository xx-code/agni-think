package dev.auguste.agni_api.core.usecases.tags

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.usecases.tags.dto.CreateTagInput

class CreateTag(private val tagRepo: IRepository<Tag>): IUseCase<CreateTagInput, CreatedOutput> {

    override fun execAsync(input: CreateTagInput): CreatedOutput {
        if (tagRepo.existsByName(input.value))
            throw DomainException.AlreadyExist.Tag(input.value)

        val newTag = Tag(value = input.value, color = input.color, isSystem =  input.isSystem.let { input.isSystem } ?: false)

        tagRepo.create(newTag)

        return CreatedOutput(newTag.id)
    }
}