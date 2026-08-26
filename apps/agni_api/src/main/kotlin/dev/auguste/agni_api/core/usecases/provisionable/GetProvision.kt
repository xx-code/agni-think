package dev.auguste.agni_api.core.usecases.provisionable

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.provisionable.dto.GetProvisionOutput
import dev.auguste.agni_api.core.value_objects.Scheduler
import java.util.UUID

class GetProvision(
    private val provisionRepo: IRepository<Provision>
    ): IUseCase<UUID, GetProvisionOutput> {
    override fun execAsync(input: UUID): GetProvisionOutput {
        val provisionable = provisionRepo.get(input) ?: throw DomainException.NotFound.Provisionable(input)

        return GetProvisionOutput(
            id = provisionable.id,
            title = provisionable.title,
            initialCost = provisionable.initialCost,
            acquisitionDate = provisionable.acquisitionDate,
            expectedLifespanMonth = provisionable.expectedLifespanMonth,
            totalCost = provisionable.calculateTotalCost(),
            costByMonth = provisionable.calculateTotalCostPerMonth(),
            monthlyPayment = provisionable.calculateMonthlyPayment(),
            residualValue = provisionable.calculateResidualValue(),
            nextPaymentDate = provisionable.paymentInfo?.scheduler?.date?.toLocalDate(),
            nextPaymentAmount = provisionable.paymentInfo?.paymentAmount,
        )
    }
}