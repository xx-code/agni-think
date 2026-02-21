package dev.auguste.agni_api.core.usecases.finance_principles

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.FinancePrinciple
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.finance_principles.dto.GetFinancePrincipleOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class GetAllFinancePrinciple(
    val financePrincipleRepo: IRepository<FinancePrinciple>
) : IUseCase<QueryFilter, ListOutput<GetFinancePrincipleOutput>> {
    override fun execAsync(input: QueryFilter): ListOutput<GetFinancePrincipleOutput> {
        val financePrincipes = financePrincipleRepo.getAll(input)

        return ListOutput(
            items = financePrincipes.items.map { GetFinancePrincipleOutput(
                id = it.id,
                description = it.description,
                targetType = it.targetType.value,
                logicRules = it.logicRules,
                name = it.name,
                strictness = it.strictness,
            ) },
            total = financePrincipes.total
        )
    }
}