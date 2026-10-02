package dev.auguste.agni_api.controllers.models

import domain.enums.DepreciationType
import domain.enums.PeriodType
import domain.enums.ProvisionType
import usecases.provisionable.dto.CreateProvisionInput
import usecases.provisionable.dto.ScheduleInvoiceProvisionInput
import usecases.provisionable.dto.UpdateProvisionInput
import domain.value_objects.ProvisionDepreciateCriteria
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.util.UUID

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

    @field:DecimalMin(value = "0.0", message = "Cost HT must be positive")
    val costHT: Double,

    @field:DecimalMin(value = "0.0", message = "Cost TTC must be positive")
    val costTTC: Double,

    @field:NotNull(message = "Acquisition date must be set")
    val acquisitionDate: LocalDate,

    @field:Min(0, message = "Expected lifespan (month) must be positive")
    val expectedLifespanMonth: Int,

    @field:NotNull(message = "Provision type must be set")
    val type: String,

    val isPatrimony: Boolean = false,

    @field:NotEmpty(message = "Depreciation criteria must not be empty")
    val depreciationCriteria: List<ApiProvisionDepreciateCriteriaInput>,

    val fundAmortizationId: UUID? = null,
    val scheduleInvoice: ApiScheduleInvoiceProvisionModel? = null,
    val isInstallmentOnTTC: Boolean = true,

    @field:DecimalMin(value = "0.0", message = "Floor value must be positive")
    val floorValue: Double = 0.0,

    @field:DecimalMin(value = "0.0", message = "Interest loan must be positive")
    val interestLoan: Double = 0.0,

    @field:Min(0, message = "Loan month must be positive")
    val loanMonth: Int = 0
)

data class ApiUpdateProvisionModel(
    val title: String?,

    @field:DecimalMin(value = "0.0", message = "Cost HT must be positive")
    val costHT: Double,

    @field:DecimalMin(value = "0.0", message = "Cost TTC must be positive")
    val costTTC: Double,

    val acquisitionDate: LocalDate?,

    @field:Min(0, message = "Expected lifespan (month) must be positive")
    val expectedLifespanMonth: Int?,

    val isPatrimony: Boolean?,
    val isInstallmentOnTTC: Boolean = true,

    val scheduleInvoice: ApiScheduleInvoiceProvisionModel?,

    val depreciationCriteria: List<ApiProvisionDepreciateCriteriaInput>?,

    val fundAmortizationId: UUID? = null,
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

    val tagIds: Set<UUID> = setOf(),
    val budgetIds: Set<UUID> = setOf(),
    val paymentPeriod: String,
    val paymentInterval: Int,
)

fun mapApiScheduleInvoiceProvision(model: ApiScheduleInvoiceProvisionModel): ScheduleInvoiceProvisionInput {
    return ScheduleInvoiceProvisionInput(
        invoiceAccountId = model.invoiceAccountId,
        invoiceCategoryId = model.invoiceCategoryId,
        tagIds = model.tagIds,
        budgetIds = model.budgetIds,
        paymentPeriod = PeriodType.fromString(model.paymentPeriod),
        paymentInterval = model.paymentInterval,
    )
}

fun mapApiCreateProvision(model: ApiCreateProvisionModel): CreateProvisionInput {
    return CreateProvisionInput(
        title = model.title,
        costHT = model.costHT,
        costTTC = model.costTTC,
        acquisitionDate = model.acquisitionDate,
        isInstallmentOnTTC = model.isInstallmentOnTTC,
        expectedLifespanMonth = model.expectedLifespanMonth,
        type = ProvisionType.fromString(model.type),
        isPatrimony = model.isPatrimony,
        fundAmortizationId = model.fundAmortizationId,
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
        costHT = model.costHT,
        costTTC = model.costTTC,
        acquisitionDate = model.acquisitionDate,
        expectedLifespanMonth = model.expectedLifespanMonth,
        isPatrimony = model.isPatrimony,
        isInstallmentOnTTC = model.isInstallmentOnTTC,
        fundAmortizationId = model.fundAmortizationId,
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