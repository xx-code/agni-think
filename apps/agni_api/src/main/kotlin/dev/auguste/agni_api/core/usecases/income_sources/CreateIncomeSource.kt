package dev.auguste.agni_api.core.usecases.income_sources

import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterInput
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.IncomeSource
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.income_sources.dto.CreateIncomeSourceInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.IncomeSourceFrequencyType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.entities.enums.ScheduleInvoiceModuleLinkerType
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.SchedulerInvoiceInput
import dev.auguste.agni_api.core.value_objects.ScheduleInvoiceModuleLinker
import dev.auguste.agni_api.infras.persistences.JdbcUnitOfWork

class CreateIncomeSource(
    private val incomeSourceRepo: IRepository<IncomeSource>,
    private val createScheduleInvoice: IUseCase<CreateScheduleInvoiceInput, CreatedOutput>,
    private val accountRepo: IRepository<Account>,
    private val unitOfWork: IUnitOfWork,
) : IUseCase<CreateIncomeSourceInput, CreatedOutput> {
    override fun execAsync(input: CreateIncomeSourceInput): CreatedOutput {
        return unitOfWork.execute {
            if (incomeSourceRepo.existsByName(input.title))
                throw DomainException.AlreadyExist.IncomeSource(input.title)

            if (input.linkedAccountId != null) {
                if (accountRepo.get(input.linkedAccountId) == null)
                    throw DomainException.NotFound.Account(input.linkedAccountId)
            }

            if (input.reliabilityLevel !in 0..100)
                throw DomainException.BusinessLogic.InvalidReliabilityLevel(input.reliabilityLevel)

            val newIncomeSource = IncomeSource(
                title = input.title,
                type = input.type,
                payFrequency = input.payFrequencyType,
                reliabilityLevel = input.reliabilityLevel,
                startDate = input.startDate,
                taxRate = input.taxRate,
                otherRate = input.otherRate,
                linkedAccountId = input.linkedAccountId,
                annualGrossAmount = input.annualGrossAmount,
                endDate = input.endDate,
            )

            incomeSourceRepo.create(newIncomeSource)

            if (newIncomeSource.linkedAccountId != null && input.invoiceIncomeCategoryId != null) {
                createScheduleInvoice.execAsync(CreateScheduleInvoiceInput(
                    accountId = newIncomeSource.linkedAccountId!!,
                    amount = newIncomeSource.getEstimateNextNetAmount(),
                    description = newIncomeSource.title,
                    categoryId = input.invoiceIncomeCategoryId,
                    tagIds = setOf(),
                    type = InvoiceType.INCOME,
                    schedule = SchedulerInvoiceInput(
                        dueDate = newIncomeSource.getEstimateNextDate().atStartOfDay(),
                        repeater = ScheduleRepeaterInput(
                            period = when(newIncomeSource.payFrequency) {
                                IncomeSourceFrequencyType.BIWEEKLY -> PeriodType.WEEK
                                IncomeSourceFrequencyType.MONTHLY -> PeriodType.MONTH
                                IncomeSourceFrequencyType.YEARLY -> PeriodType.YEAR
                                else -> PeriodType.MONTH
                            },
                            interval = when(newIncomeSource.payFrequency) {
                                IncomeSourceFrequencyType.BIWEEKLY -> 2
                                else -> 1
                            }
                        )
                    ),
                    isFreeze = false,
                    freezeSchedule = null,
                    moduleLinker = ScheduleInvoiceModuleLinker(newIncomeSource.id, ScheduleInvoiceModuleLinkerType.INCOME_SOURCE),
                    endDate = input.endDate?.atStartOfDay()
                ))
            }

            CreatedOutput(newIncomeSource.id)
        }
    }
}