package dev.auguste.agni_api.core.usecases.tags

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class DeleteTag(val tagRepo: IRepository<Tag>): IUseCase<UUID, Unit> {

    override fun execAsync(input: UUID): Unit {
        tagRepo.get(input) ?: throw Error("Tag with name $input not found.")

        tagRepo.delete(input)
    }
}