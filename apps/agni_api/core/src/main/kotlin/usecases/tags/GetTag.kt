package usecases.tags

import adapters.repositories.IRepository
import domain.entities.Tag
import usecases.interfaces.IUseCase
import domain.exceptions.NotFoundException
import usecases.tags.dto.GetTagOutput
import java.util.UUID

class GetTag(private val tagRepo: IRepository<Tag>): IUseCase<UUID, GetTagOutput> {

    override fun execAsync(input: UUID): GetTagOutput {
        val tag = tagRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "tag")

        return GetTagOutput(
            id = tag.id,
            value = tag.value + if (tag.isArchived) " (Archiver)" else "",
            color = tag.color.toString(),
            isSystem = tag.isSystem,
            isArchived = tag.isArchived,
        )
    }
}