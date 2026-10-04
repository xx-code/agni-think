package usecases.income_sources

import adapters.dto.QueryFilter
import adapters.dto.ScheduleRepeaterInput
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Account
import domain.entities.IncomeSource
import usecases.income_sources.dto.UpdateIncomeSourceInput
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.ScheduleInvoice
import domain.enums.IncomeSourceFrequencyType
import domain.enums.PeriodType
import domain.enums.ScheduleInvoiceModuleLinkerType
import usecases.UseCase
import usecases.interfaces.IUseCase
import usecases.schedule_Invoices.dto.SchedulerInvoiceInput
import usecases.schedule_Invoices.dto.UpdateScheduleInvoiceInput

class UpdateIncomeSource(
    private val incomeSourceRepo: IRepository<IncomeSource>,
    private val accountRepo: IRepository<Account>,
    private val updateScheduleInvoice: IUseCase<UpdateScheduleInvoiceInput, Unit>,
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    unitOfWork: IUnitOfWork
): UseCase<UpdateIncomeSourceInput, Unit>(unitOfWork) {
    override suspend fun process(input: UpdateIncomeSourceInput) {
        val incomeSource = incomeSourceRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "income_source")

        if (input.title != null) {
            if (input.title != incomeSource.title && incomeSourceRepo.existsByName(input.title)) {
                throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "income_source")
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
                throw NotFoundException.SingleEntity(input.linkedAccountId, "account")

            incomeSource.linkedAccountId = input.linkedAccountId
        }

        if (input.annualGrossAmount != null)
            incomeSource.annualGrossAmount = input.annualGrossAmount

        if (input.reliabilityLevel != null) {
            if (input.reliabilityLevel !in 0..100)
                throw ValidationException.InvalidReliabilityLevel(input.reliabilityLevel)

            incomeSource.reliabilityLevel = input.reliabilityLevel
        }

        if (incomeSource.hasChanged())
            incomeSourceRepo.update(incomeSource)

        val scheduleInvoiceCondition = QueryExtendBuilder<ScheduleInvoice>()
            .addCondition("moduleLinker.sourceId", QueryComparator.Equal, incomeSource.id)
            .addCondition("moduleLinker.module", QueryComparator.Equal, ScheduleInvoiceModuleLinkerType.INCOME_SOURCE.value)

        val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll(), scheduleInvoiceCondition)
        if (scheduleInvoices.items.isNotEmpty()) {
            updateScheduleInvoice.processDirect(UpdateScheduleInvoiceInput(
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