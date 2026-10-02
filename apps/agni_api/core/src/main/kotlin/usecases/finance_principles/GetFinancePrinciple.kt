package usecases.finance_principles

import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.FinancePrinciple
import usecases.finance_principles.dto.GetFinancePrincipleOutput
import usecases.interfaces.IUseCase
import java.util.UUID

class GetFinancePrinciple(
    private val financePrincipleRepo: IRepository<FinancePrinciple>
) : IUseCase<UUID, GetFinancePrincipleOutput> {
    override fun execAsync(input: UUID): GetFinancePrincipleOutput {
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