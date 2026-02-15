package dev.auguste.agni_api.core.usecases.provisionable

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.provisionable.dto.UpdateProvisionInput

class UpdateProvisionable(
    private val provisionRepo: IRepository<Provision>
): IUseCase<UpdateProvisionInput, Unit> {
    override fun execAsync(input: UpdateProvisionInput) {
        val provisionable = provisionRepo.get(input.id) ?: throw Error("Provisionable not found")


        if (input.title != null) {
            if (input.title != provisionable.title && provisionRepo.existsByName(input.title))
                throw Error("Provisionable already exist")

            provisionable.title = input.title
        }

        if (input.initialCost != null) {
            if (input.initialCost <= 0)
                throw Error("Provisionable initial cannot be greater than 0")

            provisionable.initialCost = input.initialCost
        }

        if (input.expectedLifespanMonth != null) {
            if (input.expectedLifespanMonth <= 0)
                throw Error("Provisionable initial cannot be greater than 0")

            provisionable.expectedLifespanMonth = input.expectedLifespanMonth
        }

        if (input.residualValue != null) {
            if (input.residualValue < 0)
                throw Error("Provisionable residualValue must be greater than 0")

            provisionable.residualValue = input.residualValue
        }

        if (input.acquisitionDate != null) {
            provisionable.acquisitionDate = input.acquisitionDate
        }

        if (provisionable.hasChanged())
            provisionRepo.update(provisionable)
    }
}