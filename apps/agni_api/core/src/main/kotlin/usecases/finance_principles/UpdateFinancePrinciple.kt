package usecases.finance_principles

import usecases.UseCase
import adapters.repositories.IRepository
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.FinancePrinciple
import usecases.finance_principles.dto.UpdateFinancePrincipleInput
class UpdateFinancePrinciple(
    private val financePrincipleRepo: IRepository<FinancePrinciple>
): UseCase<UpdateFinancePrincipleInput, Unit>() {
    override suspend fun process(input: UpdateFinancePrincipleInput) {
        val financePrinciple = financePrincipleRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "finance_principle")

        if (input.name != null) {
            if (input.name != financePrinciple.name && financePrincipleRepo.existsByName(input.name))
                throw AlreadyExistException.EntitiesByField(mapOf("name" to input.name), "finance_principle")

            financePrinciple.name = input.name
        }

        if (input.targetType != null)
            financePrinciple.targetType = input.targetType

        if (input.logicRules != null)
            financePrinciple.logicRules = input.logicRules

        if (input.strictness != null) {
            if (input.strictness !in 1..10)
                throw ValidationException.FinancePrincipleNameLengthInvalid()

            financePrinciple.strictness = input.strictness
        }

        if (input.description != null)
            financePrinciple.description = input.description

        if (financePrinciple.hasChanged())
            financePrincipleRepo.update(financePrinciple)
    }
}