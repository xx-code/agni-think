package dev.auguste.agni_api.core.usecases.finance_principles

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.FinancePrinciple
import dev.auguste.agni_api.core.usecases.finance_principles.dto.UpdateFinancePrincipleInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class UpdateFinancePrinciple(
    private val financePrincipleRepo: IRepository<FinancePrinciple>
): IUseCase<UpdateFinancePrincipleInput, Unit> {
    override fun execAsync(input: UpdateFinancePrincipleInput) {
        val financePrinciple = financePrincipleRepo.get(input.id) ?: throw Error("FinancePrinciple ${input.id} does not exist")

        if (input.name != null) {
            if (input.name != financePrinciple.name && financePrincipleRepo.existsByName(input.name))
                throw Error("FinancePrinciple ${input.name} already exists")

            financePrinciple.name = input.name
        }

        if (input.targetType != null)
            financePrinciple.targetType = input.targetType

        if (input.logicRules != null)
            financePrinciple.logicRules = input.logicRules

        if (input.strictness != null) {
            if (input.strictness !in 1..10)
                throw Error("Finance principle name must be between 1 and 10.")

            financePrinciple.strictness = input.strictness
        }

        if (input.description != null)
            financePrinciple.description = input.description

        if (financePrinciple.hasChanged())
            financePrincipleRepo.update(financePrinciple)
    }
}