package dev.auguste.agni_api.core.usecases.finance_principles

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.FinancePrinciple
import dev.auguste.agni_api.core.usecases.finance_principles.dto.GetFinancePrincipleOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class GetFinancePrinciple(
    private val financePrincipleRepo: IRepository<FinancePrinciple>
) : IUseCase<UUID, GetFinancePrincipleOutput> {
    override fun execAsync(input: UUID): GetFinancePrincipleOutput {
        val financePrinciple = financePrincipleRepo.get(input) ?: throw Error("FinancePrinciple $input not found")

        return GetFinancePrincipleOutput(
            id = financePrinciple.id,
            name = financePrinciple.name,
            description = financePrinciple.description,
            targetType = financePrinciple.targetType.value,
            strictness = financePrinciple.strictness,
            logicRules = financePrinciple.logicRules
        )
    }
}