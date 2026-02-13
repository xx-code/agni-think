package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateFreezeInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.InvoiceDeductionInput
import dev.auguste.agni_api.core.usecases.invoices.dto.TransactionInput
import dev.auguste.agni_api.core.usecases.invoices.dto.TransferInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.UpdateInvoiceInput
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

data class ApiQueryInvoice(
    val accountIds: Set<UUID>? = null,
    @field:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val startDate: LocalDateTime? = null,

    @field:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val endDate: LocalDateTime? = null,
    val status: String? = null,
    val types: Set<String>? = null,
    val isFreeze: Boolean? = null,
    val mouvement: String? = null,
    val categoryIds: Set<UUID>? = null,
    val tagIds: Set<UUID>? = null,
    val budgetIds: Set<UUID>? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null
)

data class ApiQueryBalanceByPeriod(
    val period: String = "Day",
    val interval: Int = 1,

    @field:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val dateFrom: LocalDateTime = LocalDateTime.now(),

    val status: String? = null,

    @field:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val dateTo: LocalDateTime? = null,

    val accountIds: Set<UUID>? = null,
    val mouvement: String? = null,
    val types: Set<String>? = null,
    val isFreeze: Boolean? = null,
    val categoryIds: Set<UUID>? = null,
    val tagIds: Set<UUID>? = null,
    val budgetIds: Set<UUID>? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null

)

data class ApiTransactionModel(
    @field:Min(value = 0, message = "The transaction amount cannot be more than 0")
    val amount: Double,
    @field:NotNull(message = "The categoryId value cannot be null")
    val categoryId: UUID,
    @field:NotEmpty(message = "The description value cannot be null")
    val description: String,
    val tagIds: Set<UUID>,
    val budgetIds: Set<UUID>
)

data class ApiDeductionModel(
    @field:NotNull(message = "The deductionId cannot be null")
    val deductionId: UUID,
    @field:NotNull(message = "The amount cannot be null")
    val amount: Double
)

data class ApiCreateInvoiceModel(
    @field:NotNull(message = "The accountId cannot be null")
    val accountId: UUID,
    @field:NotNull(message = "The invoice status cannot be null")
    val status: String,
    @field:NotNull(message = "The date cannot be null")
    val date: LocalDateTime,
    @field:NotNull(message = "The invoice type cannot be null")
    val type: String,
    @field:NotNull(message = "The invoice mouvement cannot be null")
    val mouvement: String,
    val currencyId: UUID?,
    val transactions: Set<ApiTransactionModel>,
    val deductions: Set<ApiDeductionModel>
)

data class ApiUpdateInvoiceModel(
    val accountId: UUID?,
    val date: LocalDateTime?,
    val type: String,
    val mouvement: String?,
    val currencyId: UUID?,
    val addTransactions: Set<ApiTransactionModel>,
    val removeTransactionIds: Set<UUID>,
    val deductions: Set<ApiDeductionModel>
)

data class ApiTransferInvoiceModel(
    val accountIdFrom: UUID,
    val accountIdTo: UUID,
    val date: LocalDateTime,
    val amount: Double
)

data class ApiCreateFreezeInvoiceModel(
    val accountId: UUID,
    val endDate: LocalDateTime,
    val title: String,
    val amount: Double,
    val status: String
)

fun mapApiCreateInvoice(model: ApiCreateInvoiceModel): CreateInvoiceInput {
    return CreateInvoiceInput(
        accountId = model.accountId,
        status = InvoiceStatusType.fromString(model.status)  ,
        date = model.date,
        type = InvoiceType.fromString(model.type),
        mouvementType = InvoiceMouvementType.fromString(model.mouvement) ,
        currency = model.currencyId,
        transactions = model.transactions.map {
            TransactionInput(
                amount = it.amount,
                categoryId = it.categoryId,
                description = it.description,
                tagIds = it.tagIds,
                budgetIds = it.budgetIds
            )
        }.toSet(),
        deductions = model.deductions.map {
            InvoiceDeductionInput(
                deductionId = it.deductionId,
                amount = it.amount
            )
        }.toSet()
    )
}

fun mapApiUpdateInvoice(id: UUID, model: ApiUpdateInvoiceModel): UpdateInvoiceInput {
    return UpdateInvoiceInput(
        id = id,
        accountId = model.accountId,
        date = model.date,
        type = InvoiceType.fromString(model.type),
        mouvementType = model.mouvement?.let { InvoiceMouvementType.fromString(model.mouvement) } ,
        currency = model.currencyId,
        removeTransactionIds = model.removeTransactionIds,
        addTransactions = model.addTransactions.map {
            TransactionInput(
                amount = it.amount,
                categoryId = it.categoryId,
                description = it.description,
                tagIds = it.tagIds,
                budgetIds = it.budgetIds
            )
        }.toSet(),
        deductions = model.deductions.map {
            InvoiceDeductionInput(
                deductionId = it.deductionId,
                amount = it.amount
            )
        }.toSet()
    )
}

fun mapApiTransfer(model: ApiTransferInvoiceModel): TransferInvoiceInput {
    return TransferInvoiceInput(
        accountIdFrom = model.accountIdFrom,
        accountIdTo = model.accountIdTo,
        date = model.date,
        amount = model.amount
    )
}

fun mapApiCreateFreezeInvoice(model: ApiCreateFreezeInvoiceModel) : CreateFreezeInvoiceInput {
    return CreateFreezeInvoiceInput(
        accountId = model.accountId,
        endDate = model.endDate,
        title = model.title,
        amount = model.amount,
        status = InvoiceStatusType.fromString(model.status)
    )
}