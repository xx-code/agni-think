package usecases.spending_period_template

import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.SpendingPeriodTemplate
import usecases.interfaces.IUseCase
import java.util.UUID

class DeleteSpendingPeriodTemplate(
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
): IUseCase<UUID, Unit> {
    override fun execAsync(input: UUID) {
        spendingPeriodTemplateRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "spending_period_template")

        spendingPeriodTemplateRepo.delete(input)
    }
}