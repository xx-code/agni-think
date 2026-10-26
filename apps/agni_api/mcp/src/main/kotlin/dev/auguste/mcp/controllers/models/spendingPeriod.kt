package dev.auguste.mcp.controllers.models

import domain.enums.SpendingPeriodStateType
import org.springframework.ai.mcp.annotation.McpToolParam
import usecases.spending_period.dto.CreateSpendingPeriodInput
import usecases.spending_period.dto.GetAllSpendingPeriodInput
import usecases.spending_period.dto.UpdateSpendingPeriodInput
import java.time.LocalDate
import java.util.UUID

data class SpendingPeriodSearchModel(
    @field:McpToolParam(
        description = "Pagination and sorting options. Omit this field to get the first 15 spending periods. Sort by \"startDate\" or \"endDate\" to find the current period.",
        required = false,
    )
    val page: PageQueryModel = PageQueryModel(),

    @field:McpToolParam(
        description = "Optional UUID of a spending period template. Only periods created from this template are returned.",
        required = false,
    )
    val spendingPeriodTemplateId: UUID? = null,

    @field:McpToolParam(
        description = "Optional state filter. Accepted values: \"Draft\", \"ToReview\", \"InProgress\", \"Complete\" (case-insensitive).",
        required = false,
    )
    val state: String? = null,
) {
    fun toDomain(): GetAllSpendingPeriodInput {
        return GetAllSpendingPeriodInput(
            queryFilter = page.toDomain(),
            spendingPeriodTemplateId = spendingPeriodTemplateId,
            state = state?.let { SpendingPeriodStateType.fromString(it) },
        )
    }
}

data class CreateSpendingPeriodModel(
    @field:McpToolParam(
        description = "UUID of the spending period template used to create this budget period.",
    )
    val spendingPeriodTemplateId: UUID,

    @field:McpToolParam(
        description = "Target saving rate as a percentage of income, for example 20.0 for 20 percent. Use 0 when the user does not want to save.",
    )
    val savingRateTarget: Double,

    @field:McpToolParam(
        description = "Initial state. Accepted values: \"Draft\", \"ToReview\", \"InProgress\", \"Complete\". Usually omit it and let the period start as Draft.",
        required = false,
    )
    val state: String = SpendingPeriodStateType.DRAFT.value,

    @field:McpToolParam(
        description = "First day of the period in ISO-8601 date format, for example \"2026-10-01\". Defaults to today.",
        required = false,
    )
    val startDate: LocalDate = LocalDate.now(),

    @field:McpToolParam(
        description = "Last day of the period in ISO-8601 date format, for example \"2026-10-31\". Defaults to today.",
        required = false,
    )
    val endDate: LocalDate = LocalDate.now(),

    @field:McpToolParam(
        description = "UUIDs of the scheduled incomes to include in this period. Send an empty list when none apply.",
        required = false,
    )
    val scheduleIncomeSelectedIds: Set<UUID> = emptySet(),

    @field:McpToolParam(
        description = "UUIDs of the scheduled expenses to include in this period. Send an empty list when none apply.",
        required = false,
    )
    val scheduleExpenseSelectedIds: Set<UUID> = emptySet(),

    @field:McpToolParam(
        description = "UUIDs of the budgets to include in this period. Send an empty list when none apply.",
        required = false,
    )
    val budgetsSelectedIds: Set<UUID> = emptySet(),

    @field:McpToolParam(
        description = "UUIDs of the wishlist items to include in this period. Send an empty list when none apply.",
        required = false,
    )
    val wishlistSelectedIds: Set<UUID> = emptySet(),
) {
    fun toDomain(): CreateSpendingPeriodInput {
        return CreateSpendingPeriodInput(
            spendingPeriodTemplateId = spendingPeriodTemplateId,
            startDate = startDate,
            endDate = endDate,
            savingRateTarget = savingRateTarget,
            state = SpendingPeriodStateType.fromString(state),
            scheduleIncomesSelected = scheduleIncomeSelectedIds,
            scheduleExpensesSelected = scheduleExpenseSelectedIds,
            budgetsSelected = budgetsSelectedIds,
            wishlistItemSelected = wishlistSelectedIds,
        )
    }
}

data class UpdateSpendingPeriodModel(
    @field:McpToolParam(
        description = "New spending period template UUID. Omit to keep the current template.",
        required = false,
    )
    val spendingPeriodTemplateId: UUID? = null,

    @field:McpToolParam(
        description = "New saving rate target as a percentage of income, for example 25.0 for 25 percent. Omit to keep the current target.",
        required = false,
    )
    val savingRateTarget: Double? = null,

    @field:McpToolParam(
        description = "New state. Accepted values: \"Draft\", \"ToReview\", \"InProgress\", \"Complete\". Omit to keep the current state.",
        required = false,
    )
    val state: String? = null,

    @field:McpToolParam(
        description = "New first day of the period in ISO-8601 date format. Omit to keep the current start date.",
        required = false,
    )
    val startDate: LocalDate? = null,

    @field:McpToolParam(
        description = "New last day of the period in ISO-8601 date format. Omit to keep the current end date.",
        required = false,
    )
    val endDate: LocalDate? = null,

    @field:McpToolParam(
        description = "Replacement list of scheduled income UUIDs. Send an empty list to remove every selected income. Omit to keep the current selection.",
        required = false,
    )
    val scheduleIncomesSelected: Set<UUID>? = null,

    @field:McpToolParam(
        description = "Replacement list of scheduled expense UUIDs. Send an empty list to remove every selected expense. Omit to keep the current selection.",
        required = false,
    )
    val scheduleExpensesSelected: Set<UUID>? = null,

    @field:McpToolParam(
        description = "Replacement list of budget UUIDs. Send an empty list to remove every selected budget. Omit to keep the current selection.",
        required = false,
    )
    val budgetsSelected: Set<UUID>? = null,

    @field:McpToolParam(
        description = "Replacement list of wishlist item UUIDs. Send an empty list to remove every selected wishlist item. Omit to keep the current selection.",
        required = false,
    )
    val wishlistSelected: Set<UUID>? = null,
) {
    fun toDomain(spendingPeriodId: UUID): UpdateSpendingPeriodInput {
        return UpdateSpendingPeriodInput(
            id = spendingPeriodId,
            spendingPeriodTemplateId = spendingPeriodTemplateId,
            startDate = startDate,
            endDate = endDate,
            savingRateTarget = savingRateTarget,
            state = state?.let { SpendingPeriodStateType.fromString(it) },
            scheduleIncomesSelected = scheduleIncomesSelected,
            scheduleExpensesSelected = scheduleExpensesSelected,
            budgetsSelected = budgetsSelected,
            wishlistItemSelected = wishlistSelected,
        )
    }
}
