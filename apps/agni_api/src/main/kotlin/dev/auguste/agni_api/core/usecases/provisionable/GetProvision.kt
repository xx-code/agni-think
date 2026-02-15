package dev.auguste.agni_api.core.usecases.provisionable

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.provisionable.dto.GetProvisionableOutput
import java.util.UUID

class GetProvision(
    private val provisionRepo: IRepository<Provision>
): IUseCase<UUID, GetProvisionableOutput> {
    override fun execAsync(input: UUID): GetProvisionableOutput {
        val provisionable = provisionRepo.get(input) ?: throw Error("Provisionable Not Found")

        return GetProvisionableOutput(
            id = provisionable.id,
            title = provisionable.title,
            initialCost = provisionable.initialCost,
            acquisitionDate = provisionable.acquisitionDate,
            expectedLifespanMonth = provisionable.expectedLifespanMonth,
            residualValue = provisionable.residualValue
        )
    }
}