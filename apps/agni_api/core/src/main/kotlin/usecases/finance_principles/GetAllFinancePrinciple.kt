package usecases.finance_principles

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.FinancePrinciple
import usecases.ListOutput
import usecases.finance_principles.dto.GetFinancePrincipleOutput
import usecases.interfaces.IUseCase

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