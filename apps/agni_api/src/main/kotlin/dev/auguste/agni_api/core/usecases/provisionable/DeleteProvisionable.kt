package dev.auguste.agni_api.core.usecases.provisionable

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Provisionable
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class DeleteProvisionable(
    val provisionableRepo: IRepository<Provisionable>,
): IUseCase<UUID, Unit> {
    override fun execAsync(input: UUID) {
        provisionableRepo.get(input) ?: throw Error("Provisionable Not Found")

        provisionableRepo.delete(input)
    }
}