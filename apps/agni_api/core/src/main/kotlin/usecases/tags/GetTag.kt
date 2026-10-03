package usecases.tags

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Tag
import domain.exceptions.NotFoundException
import usecases.tags.dto.GetTagOutput
import java.util.UUID

class GetTag(private val tagRepo: IRepository<Tag>): UseCase<UUID, GetTagOutput>() {

    override suspend fun process(input: UUID): GetTagOutput {
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