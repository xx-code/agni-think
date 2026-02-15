package dev.auguste.agni_api.core.usecases.provisionable

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.provisionable.dto.CreateProvisionableInput

class CreateProvisionable(
    private val provisionRepo: IRepository<Provision>,
) : IUseCase<CreateProvisionableInput, CreatedOutput> {
    override fun execAsync(input: CreateProvisionableInput): CreatedOutput {
        if (provisionRepo.existsByName(input.title))
            throw Error("Provisionable already exist")

        if (input.initialCost <= 0)
            throw Error("Provisionable initial doesn't have a cost")

        if (input.expectedLifespanMonth <= 0)
            throw Error("Provisionable initial doesn't have a life month")

        if (input.residualValue < 0)
            throw Error("Provisionable residualValue must be greater than 0")

        val provision = Provision(
            title = input.title,
            initialCost = input.initialCost,
            acquisitionDate = input.acquisitionDate,
            expectedLifespanMonth = input.expectedLifespanMonth,
            residualValue = input.residualValue
        )

        provisionRepo.create(provision)

        return CreatedOutput(provision.id)
    }
}