package usecases.tags

import adapters.repositories.IRepository
import domain.entities.Color
import domain.entities.Tag
import usecases.CreatedOutput
import usecases.interfaces.IUseCase
import domain.exceptions.AlreadyExistException
import usecases.tags.dto.CreateTagInput

class CreateTag(private val tagRepo: IRepository<Tag>): IUseCase<CreateTagInput, CreatedOutput> {

    override fun execAsync(input: CreateTagInput): CreatedOutput {
        if (tagRepo.existsByName(input.value))
            throw AlreadyExistException.EntitiesByField(mapOf("name" to input.value), "tag")

        val newTag = Tag(value = input.value, color = Color(input.color), isSystem =  input.isSystem.let { input.isSystem } ?: false)

        tagRepo.create(newTag)

        return CreatedOutput(newTag.id)
    }
}