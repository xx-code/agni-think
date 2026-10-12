package usecases.spending_period_template

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.SpendingPeriodTemplate
import java.util.UUID

class DeleteSpendingPeriodTemplate(
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
    auth: IAuthenticateAdapter
): UseCase<UUID, Unit>(auth = auth) {
    override suspend fun process(input: UUID) {
        spendingPeriodTemplateRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "spending_period_template")

        spendingPeriodTemplateRepo.delete(input)
    }
}