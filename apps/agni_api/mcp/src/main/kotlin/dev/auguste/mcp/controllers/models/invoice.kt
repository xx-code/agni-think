package dev.auguste.mcp.controllers.models

import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceType
import org.springframework.ai.mcp.annotation.McpToolParam
import usecases.invoices.dto.CreateInvoiceInput
import usecases.invoices.dto.GetAllInvoiceInput
import usecases.invoices.dto.InvoiceDeductionInput
import usecases.invoices.dto.TransactionInput
import java.time.LocalDateTime
import java.util.UUID

data class InvoiceSearchModel(
    @field:McpToolParam(
        description = "Pagination and sorting options. Omit this field to get the first 15 invoices. For invoice avoid QueryAll = true",
        required = false,
    )
    val page: PageQueryModel = PageQueryModel(),

    @field:McpToolParam(
        description = "Optional list of account UUIDs. Only invoices belonging to these accounts are returned.",
        required = false,
    )
    val accountIds: List<UUID>? = null,

    @field:McpToolParam(
        description = "Optional lower bound for the invoice date in ISO-8601 date-time format, for example \"2026-01-01T00:00:00\".",
        required = false,
    )
    val startDate: LocalDateTime? = null,

    @field:McpToolParam(
        description = "Optional upper bound for the invoice date in ISO-8601 date-time format, for example \"2026-12-31T23:59:59\".",
        required = false,
    )
    val endDate: LocalDateTime? = null,

    @field:McpToolParam(
        description = "Optional status filter. Accepted values: \"Pending\" or \"Complete\" (case-insensitive).",
        required = false,
    )
    val status: String? = null,

    @field:McpToolParam(
        description = "Optional invoice type filter. Accepted values: \"Income\", \"FixedCost\", \"VariableCost\", \"Other\". Send a list to include several types.",
        required = false,
    )
    val types: List<String>? = null,

    @field:McpToolParam(
        description = "Optional freeze filter. true returns only frozen invoices, false returns only non-frozen invoices, null or omitted returns both.",
        required = false,
    )
    val isFreeze: Boolean? = null,

    @field:McpToolParam(
        description = "Optional money direction filter. Accepted values: \"Credit\" (money in) or \"Debit\" (money out).",
        required = false,
    )
    val movementType: String? = null,

    @field:McpToolParam(
        description = "Optional list of category UUIDs used to filter invoice transactions.",
        required = false,
    )
    val categoryIds: List<UUID>? = null,

    @field:McpToolParam(
        description = "Optional list of tag UUIDs used to filter invoice transactions.",
        required = false,
    )
    val tagIds: List<UUID>? = null,

    @field:McpToolParam(
        description = "Optional list of budget UUIDs used to filter invoice transactions.",
        required = false,
    )
    val budgetIds: List<UUID>? = null,

    @field:McpToolParam(
        description = "Optional minimum invoice amount, inclusive.",
        required = false,
    )
    val minAmount: Double? = null,

    @field:McpToolParam(
        description = "Optional maximum invoice amount, inclusive.",
        required = false,
    )
    val maxAmount: Double? = null,
) {
    fun toDomain(): GetAllInvoiceInput {
        return GetAllInvoiceInput(
            queryFilter = page.toDomain(),
            accountIds = accountIds?.toSet(),
            startDate = startDate,
            endDate = endDate,
            status = status?.let { InvoiceStatusType.fromString(it) },
            types = types?.map { InvoiceType.fromString(it) }?.toSet(),
            isFreeze = isFreeze,
            movementType = movementType?.let { InvoiceMovementType.fromString(it) },
            categoryIds = categoryIds?.toSet(),
            tagIds = tagIds?.toSet(),
            budgetIds = budgetIds?.toSet(),
            minAmount = minAmount,
            maxAmount = maxAmount,
        )
    }
}

data class CreateInvoiceTransactionModel(
    @field:McpToolParam(
        description = "Line amount in the invoice currency. Must be greater than 0.",
    )
    val amount: Double,

    @field:McpToolParam(
        description = "UUID of the category that classifies this line, for example the Groceries category.",
    )
    val categoryId: UUID,

    @field:McpToolParam(
        description = "Short description of the line item, for example \"Groceries at Metro\".",
    )
    val description: String,

    @field:McpToolParam(
        description = "Optional list of tag UUIDs to attach to this line. Send an empty list when there are no tags.",
        required = false,
    )
    val tagIds: List<UUID> = emptyList(),

    @field:McpToolParam(
        description = "Optional list of budget UUIDs linked to this line. Send an empty list when no budget applies.",
        required = false,
    )
    val budgetIds: List<UUID> = emptyList(),
) {
    fun toDomain(): TransactionInput {
        return TransactionInput(
            amount = amount,
            categoryId = categoryId,
            description = description,
            tagIds = tagIds.toSet(),
            budgetIds = budgetIds.toSet(),
        )
    }
}

data class CreateInvoiceDeductionModel(
    @field:McpToolParam(
        description = "UUID of the deduction definition to apply, for example a tax or fee.",
    )
    val deductionId: UUID,

    @field:McpToolParam(
        description = "Amount deducted from the invoice total in the invoice currency.",
    )
    val amount: Double,
) {
    fun toDomain(): InvoiceDeductionInput {
        return InvoiceDeductionInput(deductionId = deductionId, amount = amount)
    }
}

data class CreateInvoiceModel(
    @field:McpToolParam(
        description = "UUID of the bank account impacted by this invoice.",
    )
    val accountId: UUID,

    @field:McpToolParam(
        description = "Invoice date and time in ISO-8601 format, for example \"2026-10-25T14:30:00\". Defaults to now when omitted.",
        required = false,
    )
    val date: LocalDateTime? = null,

    @field:McpToolParam(
        description = "Invoice type. Accepted values: \"Income\", \"FixedCost\", \"VariableCost\", \"Other\" (case-insensitive). Example: \"VariableCost\" for groceries.",
    )
    val type: String,

    @field:McpToolParam(
        description = "Money direction. Accepted values: \"Credit\" (money received) or \"Debit\" (money spent). Example: \"Debit\" for a purchase.",
    )
    val movementType: String,

    @field:McpToolParam(
        description = "Optional currency UUID. Omit to use the account currency.",
        required = false,
    )
    val currencyId: UUID? = null,

    @field:McpToolParam(
        description = "One or more line items that make up the invoice. Each line requires amount, categoryId and description.",
    )
    val transactions: List<CreateInvoiceTransactionModel>,

    @field:McpToolParam(
        description = "Optional deductions (fees, taxes, ...) subtracted from the invoice total. Send an empty list when not applicable.",
        required = false,
    )
    val deductions: List<CreateInvoiceDeductionModel> = emptyList(),

    @field:McpToolParam(
        description = "Optional UUID of an existing invoice to reuse. Usually omit this field.",
        required = false,
    )
    val persistentInvoiceId: UUID? = null,

    @field:McpToolParam(
        description = "When true, the invoice is frozen (money reserved but not debited). Default: false.",
        required = false,
    )
    val isFreeze: Boolean = false,
) {
    fun toDomain(userId: UUID): CreateInvoiceInput {
        return CreateInvoiceInput(
            userId = userId,
            persistentInvoiceId = persistentInvoiceId,
            accountId = accountId,
            status = InvoiceStatusType.PENDING,
            date = date ?: LocalDateTime.now(),
            type = InvoiceType.fromString(type),
            movementType = InvoiceMovementType.fromString(movementType),
            currency = currencyId,
            transactions = transactions.map { it.toDomain() }.toSet(),
            deductions = deductions.map { it.toDomain() }.toSet(),
            isFreeze = isFreeze,
        )
    }
}
