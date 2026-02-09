package dev.auguste.agni_api.core.usecases.tags

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.tags.dto.DeleteTagInput
import java.util.UUID

class DeleteTag(private val tagRepo: IRepository<Tag>): IUseCase<DeleteTagInput, Unit> {

    override fun execAsync(input: DeleteTagInput): Unit {
        tagRepo.get(input.tagId) ?: throw Error("Tag with name ${input.tagId} not found.")

        tagRepo.delete(input.tagId)
    }
}