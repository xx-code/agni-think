package dev.auguste.agni_api.core.usecases.income_sources

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterInput
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.adapters.repositories.QueryExtendBuilder
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.IncomeSource
import dev.auguste.agni_api.core.usecases.income_sources.dto.UpdateIncomeSourceInput
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.IncomeSourceFrequencyType
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.entities.enums.ScheduleInvoiceModuleLinkerType
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.UpdateInvoiceInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.SchedulerInvoiceInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.UpdateScheduleInvoiceInput

class UpdateIncomeSource(
    private val incomeSourceRepo: IRepository<IncomeSource>,
    private val accountRepo: IRepository<Account>,
    private val updateScheduleInvoice: IUseCase<UpdateScheduleInvoiceInput, Unit>,
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val unitOfWork: IUnitOfWork
) : IUseCase<UpdateIncomeSourceInput, Unit> {
    override fun execAsync(input: UpdateIncomeSourceInput) {
        unitOfWork.execute {
            val incomeSource = incomeSourceRepo.get(input.id) ?: throw DomainException.NotFound.IncomeSource(input.id)

            if (input.title != null) {
                if (input.title != incomeSource.title && incomeSourceRepo.existsByName(input.title)) {
                    throw DomainException.AlreadyExist.IncomeSource(input.title)
                }

                incomeSource.title = input.title
            }

            if (input.type != null)
                incomeSource.type = input.type

            if (input.payFrequencyType != null)
                incomeSource.payFrequency = input.payFrequencyType

            if (input.otherRate != null)
                incomeSource.otherRate = input.otherRate

            if (input.taxRate != null)
                incomeSource.taxRate = input.taxRate

            if (input.startDate != null)
                incomeSource.startDate = input.startDate

            if (input.endDate != null)
                incomeSource.endDate = input.endDate

            if (input.linkedAccountId != null) {
                if (accountRepo.get(input.linkedAccountId) == null)
                    throw DomainException.NotFound.Account(input.linkedAccountId)

                incomeSource.linkedAccountId = input.linkedAccountId
            }

            if (input.annualGrossAmount != null)
                incomeSource.annualGrossAmount = input.annualGrossAmount

            if (input.reliabilityLevel != null) {
                if (input.reliabilityLevel !in 0..100)
                    throw DomainException.BusinessLogic.InvalidReliabilityLevel(input.reliabilityLevel)

                incomeSource.reliabilityLevel = input.reliabilityLevel
            }

            if (incomeSource.hasChanged())
                incomeSourceRepo.update(incomeSource)

            val scheduleInvoiceCondition = QueryExtendBuilder<ScheduleInvoice>()
                .addCondition("moduleLinker.sourceId", QueryComparator.Equal, incomeSource.id)
                .addCondition("moduleLinker.module", QueryComparator.Equal, ScheduleInvoiceModuleLinkerType.INCOME_SOURCE.value)

            val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll(), scheduleInvoiceCondition)
            if (scheduleInvoices.items.isNotEmpty()) {
                updateScheduleInvoice.execAsync(UpdateScheduleInvoiceInput(
                    id = scheduleInvoices.items.first().id,
                    name = incomeSource.title,
                    accountId = incomeSource.linkedAccountId,
                    amount = incomeSource.getEstimateNextNetAmount(),
                    categoryId = input.invoiceIncomeCategoryId,
                    tagIds = setOf(),
                    schedule = input.payFrequencyType?.let {
                        SchedulerInvoiceInput(
                            dueDate = incomeSource.getEstimateNextDate().atStartOfDay(),
                            repeater = ScheduleRepeaterInput(
                                period = when(it) {
                                    IncomeSourceFrequencyType.BIWEEKLY -> PeriodType.WEEK
                                    IncomeSourceFrequencyType.MONTHLY -> PeriodType.MONTH
                                    IncomeSourceFrequencyType.YEARLY -> PeriodType.YEAR
                                    else -> PeriodType.MONTH
                                },
                                interval = when(it) {
                                    IncomeSourceFrequencyType.BIWEEKLY -> 2
                                    else -> 1
                                }
                            )
                        )
                    },
                    endDate = incomeSource.endDate?.atStartOfDay(),
                    passContextEdit = true,
                ))
            }

        }
    }
}