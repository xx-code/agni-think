package usecases.schedule_Invoices

import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.IncomeSource
import domain.entities.Provision
import domain.entities.Fund
import usecases.interfaces.IUseCase
import domain.value_objects.ScheduleInvoiceModuleLinker

class VerifyScheduleModuleLinker(
    private val incomeSourceRepo: IRepository<IncomeSource>,
    private val fundSourceRepo: IRepository<Fund>,
    private val provisionRepo: IRepository<Provision>,
): IUseCase<ScheduleInvoiceModuleLinker, Unit> {
    override fun execAsync(input: ScheduleInvoiceModuleLinker) {
        when (input.module) {
            _root_ide_package_.domain.enums.ScheduleInvoiceModuleLinkerType.FUND -> {
                if (fundSourceRepo.get(input.sourceId) == null)
                    throw NotFoundException.SingleEntity(input.sourceId, "saving_goal")
            }
            _root_ide_package_.domain.enums.ScheduleInvoiceModuleLinkerType.PROVISION -> {
                if (provisionRepo.get(input.sourceId) == null)
                    throw NotFoundException.SingleEntity(input.sourceId, "provisionable")
            }
            _root_ide_package_.domain.enums.ScheduleInvoiceModuleLinkerType.INCOME_SOURCE -> {
                if (incomeSourceRepo.get(input.sourceId) == null)
                    throw NotFoundException.SingleEntity(input.sourceId, "income_source")
            }
        }
    }
}