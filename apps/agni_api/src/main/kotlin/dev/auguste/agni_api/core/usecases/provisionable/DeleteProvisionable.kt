package dev.auguste.agni_api.core.usecases.provisionable

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.provisionable.dto.DeleteProvisionableInput

class DeleteProvisionable(
    private val provisionRepo: IRepository<Provision>,
): IUseCase<DeleteProvisionableInput, Unit> {
    override fun execAsync(input: DeleteProvisionableInput) {
        provisionRepo.get(input.provisionableId) ?: throw Error("Provisionable Not Found")

        provisionRepo.delete(input.provisionableId)
    }
}