package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.entities.enums.DepreciationType
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.entities.enums.ProvisionType
import dev.auguste.agni_api.core.usecases.provisionable.dto.CreateProvisionInput
import dev.auguste.agni_api.core.usecases.provisionable.dto.ScheduleInvoiceProvisionInput
import dev.auguste.agni_api.core.usecases.provisionable.dto.UpdateProvisionInput
import dev.auguste.agni_api.core.value_objects.ProvisionDepreciateCriteria
import dev.auguste.agni_api.core.value_objects.Scheduler
import dev.auguste.agni_api.core.value_objects.SchedulerRecurrence
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class ApiScheduleProvisionModel(
    @field:NotNull("Due date must be null or empty")
    val dueDate: LocalDateTime,
    val repeater: ApiScheduleRepeaterModel? = null,
)

data class ApiProvisionDepreciateCriteriaInput(
    @field:NotEmpty(message = "Title depreciate must not be empty")
    val title: String,
    val description: String,
    @field:NotNull(message = "Provision type must be set")
    val type: String,
    @field:DecimalMin(value = "0.0", message = "Floor value must be positive")
    val value: Double,
    val monthRange: Int = 0
)

data class ApiCreateProvisionModel(
    @field:NotEmpty(message = "Title must not be empty")
    val title: String,

    @field:DecimalMin(value = "0.0", message = "Initial cost must be positive")
    val initialCost: Double,

    @field:NotNull(message = "Acquisition date must be set")
    val acquisitionDate: LocalDate,

    @field:Min(0, message = "Expected lifespan (month) must be positive")
    val expectedLifespanMonth: Int,

    @field:NotNull(message = "Provision type must be set")
    val type: String,

    val isPatrimony: Boolean = false,

    @field:NotEmpty(message = "Depreciation criteria must not be empty")
    val depreciationCriteria: List<ApiProvisionDepreciateCriteriaInput>,

    val scheduleInvoice: ApiScheduleInvoiceProvisionModel? = null,

    @field:DecimalMin(value = "0.0", message = "Floor value must be positive")
    val floorValue: Double = 0.0,

    @field:DecimalMin(value = "0.0", message = "Interest loan must be positive")
    val interestLoan: Double = 0.0,

    @field:Min(0, message = "Loan month must be positive")
    val loanMonth: Int = 0
)

data class ApiUpdateProvisionModel(
    val title: String?,

    @field:DecimalMin(value = "0.0", message = "Initial cost must be positive")
    val initialCost: Double?,

    val acquisitionDate: LocalDate?,

    @field:Min(0, message = "Expected lifespan (month) must be positive")
    val expectedLifespanMonth: Int?,

    val isPatrimony: Boolean?,

    val scheduleInvoice: ApiScheduleInvoiceProvisionModel?,

    val depreciationCriteria: List<ApiProvisionDepreciateCriteriaInput>?,

    val type: String?,

    @field:DecimalMin(value = "0.0", message = "Floor value must be positive")
    val floorValue: Double?,

    @field:DecimalMin(value = "0.0", message = "Interest loan must be positive")
    val interestLoan: Double?,

    @field:Min(0, message = "Loan month must be positive")
    val loanMonth: Int?
)

data class ApiScheduleInvoiceProvisionModel(
    @field:NotNull(message = "Invoice account id must be set")
    val invoiceAccountId: UUID,

    @field:NotNull(message = "Invoice category id must be set")
    val invoiceCategoryId: UUID,

    @field:NotNull(message = "Scheduler must be set")
    val scheduler: ApiScheduleProvisionModel,

    @field:NotNull(message = "End date must be set")
    val endDate: LocalDate,

    val tagIds: Set<UUID> = setOf(),
    val budgetIds: Set<UUID> = setOf()
)

fun mapApiScheduleInvoiceProvision(model: ApiScheduleInvoiceProvisionModel): ScheduleInvoiceProvisionInput {
    return ScheduleInvoiceProvisionInput(
        invoiceAccountId = model.invoiceAccountId,
        invoiceCategoryId = model.invoiceCategoryId,
        scheduler = Scheduler(model.scheduler.dueDate, model.scheduler.repeater?.let {
            SchedulerRecurrence(
                period = PeriodType.fromString(model.scheduler.repeater.period),
                interval = model.scheduler.repeater.interval
            )
        }),
        endDate = model.endDate,
        tagIds = model.tagIds,
        budgetIds = model.budgetIds
    )
}

fun mapApiCreateProvision(model: ApiCreateProvisionModel): CreateProvisionInput {
    return CreateProvisionInput(
        title = model.title,
        initialCost = model.initialCost,
        acquisitionDate = model.acquisitionDate,
        expectedLifespanMonth = model.expectedLifespanMonth,
        type = ProvisionType.fromString(model.type),
        isPatrimony = model.isPatrimony,
        depreciationCriteria = model.depreciationCriteria.map {
            ProvisionDepreciateCriteria(
                title = it.title,
                description = it.description,
                type = DepreciationType.fromString(it.type),
                value = it.value,
                monthRange = it.monthRange
            )
        },
        scheduleInvoice = model.scheduleInvoice?.let { mapApiScheduleInvoiceProvision(it) },
        floorValue = model.floorValue,
        interestLoan = model.interestLoan,
        loanMonth = model.loanMonth
    )
}

fun mapApiUpdateProvision(id: UUID, model: ApiUpdateProvisionModel): UpdateProvisionInput {
    return UpdateProvisionInput(
        id = id,
        title = model.title,
        initialCost = model.initialCost,
        acquisitionDate = model.acquisitionDate,
        expectedLifespanMonth = model.expectedLifespanMonth,
        isPatrimony = model.isPatrimony,
        scheduleInvoice = model.scheduleInvoice?.let { mapApiScheduleInvoiceProvision(it) },
        depreciationCriteria = model.depreciationCriteria?.map {
            ProvisionDepreciateCriteria(
                title = it.title,
                description = it.description,
                type = DepreciationType.fromString(it.type),
                value = it.value,
                monthRange = it.monthRange
            )
        },
        type =  model.type?.let {  ProvisionType.fromString(model.type) } ,
        floorValue = model.floorValue,
        interestLoan = model.interestLoan,
        loanMonth = model.loanMonth
    )
}