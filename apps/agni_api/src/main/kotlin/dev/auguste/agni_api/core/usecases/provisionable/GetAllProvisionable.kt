package dev.auguste.agni_api.core.usecases.provisionable

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.provisionable.dto.GetProvisionableOutput

class GetAllProvisionable(
    private val provisionRepo: IRepository<Provision>
): IUseCase<QueryFilter, ListOutput<GetProvisionableOutput>> {
    override fun execAsync(input: QueryFilter): ListOutput<GetProvisionableOutput> {
        val provisionables =  provisionRepo.getAll(input)

        return ListOutput(
            items = provisionables.items.map {
                GetProvisionableOutput(
                    id = it.id,
                    title = it.title,
                    initialCost = it.initialCost,
                    acquisitionDate = it.acquisitionDate,
                    expectedLifespanMonth = it.expectedLifespanMonth,
                    residualValue = it.residualValue
                )
            },
            total = provisionables.total,
        )

    }
}