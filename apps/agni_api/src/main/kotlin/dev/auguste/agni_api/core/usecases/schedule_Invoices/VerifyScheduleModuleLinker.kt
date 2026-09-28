package dev.auguste.agni_api.core.usecases.schedule_Invoices

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.IncomeSource
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.enums.ScheduleInvoiceModuleLinkerType
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.value_objects.ScheduleInvoiceModuleLinker

class VerifyScheduleModuleLinker(
    private val incomeSourceRepo: IRepository<IncomeSource>,
    private val fundSourceRepo: IRepository<SavingGoal>,
    private val provisionRepo: IRepository<Provision>,
): IUseCase<ScheduleInvoiceModuleLinker, Unit> {
    override fun execAsync(input: ScheduleInvoiceModuleLinker) {
        when (input.module) {
            ScheduleInvoiceModuleLinkerType.FUND -> {
                if (fundSourceRepo.get(input.sourceId) == null)
                    throw DomainException.NotFound.SavingGoal(input.sourceId)
            }
            ScheduleInvoiceModuleLinkerType.PROVISION -> {
                if (provisionRepo.get(input.sourceId) == null)
                    throw DomainException.NotFound.Provisionable(input.sourceId)
            }
            ScheduleInvoiceModuleLinkerType.INCOME_SOURCE -> {
                if (incomeSourceRepo.get(input.sourceId) == null)
                    throw DomainException.NotFound.IncomeSource(input.sourceId)
            }
        }
    }
}