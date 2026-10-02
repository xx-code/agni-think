package usecases.income_sources

import adapters.dto.ScheduleRepeaterInput
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Account
import domain.entities.IncomeSource
import usecases.CreatedOutput
import usecases.income_sources.dto.CreateIncomeSourceInput
import usecases.interfaces.IUseCase
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.enums.IncomeSourceFrequencyType
import domain.enums.InvoiceType
import domain.enums.PeriodType
import domain.enums.ScheduleInvoiceModuleLinkerType
import usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import usecases.schedule_Invoices.dto.SchedulerInvoiceInput
import domain.value_objects.ScheduleInvoiceModuleLinker

class CreateIncomeSource(
    private val incomeSourceRepo: IRepository<IncomeSource>,
    private val createScheduleInvoice: IUseCase<CreateScheduleInvoiceInput, CreatedOutput>,
    private val accountRepo: IRepository<Account>,
    private val unitOfWork: IUnitOfWork,
) : IUseCase<CreateIncomeSourceInput, CreatedOutput> {
    override fun execAsync(input: CreateIncomeSourceInput): CreatedOutput {
        return unitOfWork.execute {
            if (incomeSourceRepo.existsByName(input.title))
                throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "income_source")

            if (input.linkedAccountId != null) {
                if (accountRepo.get(input.linkedAccountId) == null)
                    throw NotFoundException.SingleEntity(input.linkedAccountId, "account")
            }

            if (input.reliabilityLevel !in 0..100)
                throw ValidationException.InvalidReliabilityLevel(input.reliabilityLevel)

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
                    type = _root_ide_package_.domain.enums.InvoiceType.INCOME,
                    schedule = SchedulerInvoiceInput(
                        dueDate = newIncomeSource.getEstimateNextDate().atStartOfDay(),
                        repeater = ScheduleRepeaterInput(
                            period = when(newIncomeSource.payFrequency) {
                                _root_ide_package_.domain.enums.IncomeSourceFrequencyType.BIWEEKLY -> _root_ide_package_.domain.enums.PeriodType.WEEK
                                _root_ide_package_.domain.enums.IncomeSourceFrequencyType.MONTHLY -> _root_ide_package_.domain.enums.PeriodType.MONTH
                                _root_ide_package_.domain.enums.IncomeSourceFrequencyType.YEARLY -> _root_ide_package_.domain.enums.PeriodType.YEAR
                                else -> _root_ide_package_.domain.enums.PeriodType.MONTH
                            },
                            interval = when(newIncomeSource.payFrequency) {
                                _root_ide_package_.domain.enums.IncomeSourceFrequencyType.BIWEEKLY -> 2
                                else -> 1
                            }
                        )
                    ),
                    isFreeze = false,
                    freezeSchedule = null,
                    moduleLinker = ScheduleInvoiceModuleLinker(newIncomeSource.id, _root_ide_package_.domain.enums.ScheduleInvoiceModuleLinkerType.INCOME_SOURCE),
                    endDate = input.endDate?.atStartOfDay()
                ))
            }

            CreatedOutput(newIncomeSource.id)
        }
    }
}