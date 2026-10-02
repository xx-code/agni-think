package usecases.tags

import adapters.repositories.IRepository
import domain.entities.Color
import domain.entities.Tag
import usecases.interfaces.IUseCase
import domain.exceptions.NotFoundException
import usecases.tags.dto.UpdateTagInput

class UpdateTag(private val tagRepo: IRepository<Tag>): IUseCase<UpdateTagInput, Unit> {

    override fun execAsync(input: UpdateTagInput) {
        val tag = tagRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "tag")

        if (input.value != null)
            tag.value = input.value

        if (input.color != null)
            tag.color = Color(input.color)

        if (input.archive != null)
            tag.isArchived = input.archive

        if (tag.hasChanged())
            tagRepo.update(tag)
    }

}