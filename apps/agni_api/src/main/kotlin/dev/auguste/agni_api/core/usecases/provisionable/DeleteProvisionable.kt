package dev.auguste.agni_api.core.usecases.provisionable

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.provisionable.dto.DeleteProvisionInput

class DeleteProvisionable(
    private val provisionRepo: IRepository<Provision>,
): IUseCase<DeleteProvisionInput, Unit> {
    override fun execAsync(input: DeleteProvisionInput) {
        provisionRepo.get(input.provisionableId) ?: throw Error("Provisionable Not Found")

        provisionRepo.delete(input.provisionableId)
    }
}