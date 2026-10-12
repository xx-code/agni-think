package usecases.finance_principles

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.repositories.IRepository
import domain.exceptions.AlreadyExistException
import domain.exceptions.ValidationException
import domain.entities.FinancePrinciple
import domain.entities.User
import usecases.dto.CreatedOutput
import usecases.finance_principles.dto.CreateFinancePrincipleInput
class CreateFinancePrinciple(
    private val financePrincipleRepo: IRepository<FinancePrinciple>,
    auth: IAuthenticateAdapter
): UseCase<CreateFinancePrincipleInput, CreatedOutput>(auth = auth) {
    override suspend fun process(input: CreateFinancePrincipleInput): CreatedOutput {
        val currentUser = getCurrentUser()

        if (financePrincipleRepo.existsByName(input.name))
            throw AlreadyExistException.EntitiesByField(mapOf("name" to input.name), "finance_principle")

        if (input.strictness !in 1..10)
            throw ValidationException.FinancePrincipleNameLengthInvalid()

        val newFinancePrinciple = FinancePrinciple(
            userId = currentUser.id,
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