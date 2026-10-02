package usecases.finance_principles

import adapters.repositories.IRepository
import domain.exceptions.AlreadyExistException
import domain.exceptions.ValidationException
import domain.entities.FinancePrinciple
import usecases.CreatedOutput
import usecases.finance_principles.dto.CreateFinancePrincipleInput
import usecases.interfaces.IUseCase

class CreateFinancePrinciple(
    private val financePrincipleRepo: IRepository<FinancePrinciple>
) : IUseCase<CreateFinancePrincipleInput, CreatedOutput> {
    override fun execAsync(input: CreateFinancePrincipleInput): CreatedOutput {
        if (financePrincipleRepo.existsByName(input.name))
            throw AlreadyExistException.EntitiesByField(mapOf("name" to input.name), "finance_principle")

        if (input.strictness !in 1..10)
            throw ValidationException.FinancePrincipleNameLengthInvalid()

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