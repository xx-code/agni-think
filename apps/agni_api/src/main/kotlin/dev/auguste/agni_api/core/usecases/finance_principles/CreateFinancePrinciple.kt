package dev.auguste.agni_api.core.usecases.finance_principles

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.FinancePrinciple
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.finance_principles.dto.CreateFinancePrincipleInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class CreateFinancePrinciple(
    private val financePrincipleRepo: IRepository<FinancePrinciple>
) : IUseCase<CreateFinancePrincipleInput, CreatedOutput> {
    override fun execAsync(input: CreateFinancePrincipleInput): CreatedOutput {
        if (financePrincipleRepo.existsByName(input.name))
            throw Error("Finance principle already exists.")

        if (input.strictness !in 1..10)
            throw Error("Finance principle name must be between 1 and 10.")

        val newFinancePrinciple = FinancePrinciple(
            name = input.name,
            description = input.description,
            strictness = input.strictness,
            targetType = input.targetType,
            logicRules = input.logicRules
        )

        financePrincipleRepo.create(newFinancePrinciple)

        return CreatedOutput(newFinancePrinciple.id)
    }
}