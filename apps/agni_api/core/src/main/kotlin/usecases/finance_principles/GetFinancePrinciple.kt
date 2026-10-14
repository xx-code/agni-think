package usecases.finance_principles

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.FinancePrinciple
import usecases.finance_principles.dto.GetFinancePrincipleOutput
import java.util.UUID

class GetFinancePrinciple(
    private val financePrincipleRepo: IRepository<FinancePrinciple>,
    auth: IAuthenticateAdapter
): UseCase<UUID, GetFinancePrincipleOutput>(auth = auth) {
    override suspend fun process(input: UUID): GetFinancePrincipleOutput {
        val financePrinciple = financePrincipleRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "finance_principle")

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