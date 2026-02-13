package dev.auguste.agni_api.core.usecases.provisionable

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Provisionable
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.provisionable.dto.DeleteProvisionableInput
import java.util.UUID

class DeleteProvisionable(
    private val provisionableRepo: IRepository<Provisionable>,
): IUseCase<DeleteProvisionableInput, Unit> {
    override fun execAsync(input: DeleteProvisionableInput) {
        provisionableRepo.get(input.provisionableId) ?: throw Error("Provisionable Not Found")

        provisionableRepo.delete(input.provisionableId)
    }
}