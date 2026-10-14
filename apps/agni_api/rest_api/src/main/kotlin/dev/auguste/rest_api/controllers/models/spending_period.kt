package dev.auguste.rest_api.controllers.models

import domain.enums.SpendingPeriodStateType
import usecases.spending_period.dto.CreateSpendingPeriodInput
import usecases.spending_period.dto.SpendingPeriodItemInput
import usecases.spending_period.dto.UpdateSpendingPeriodInput
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.util.UUID


data class ApiCreateSpendingPeriodModel(
    @field:NotNull("Spending period template id must be defined")
    val spendingPeriodTemplateId: UUID,
    @field:NotNull("Savings target must be defined")
    @field:Min(0, "Savings target must be greater than or equal to 0")
    val savingRateTarget: Double,
    @field:NotNull("State must be defined")
    val state: String,
    val scheduleIncomeSelectedIds: Set<UUID>,
    val scheduleExpenseSelectedIds: Set<UUID>,
    val budgetsSelectedIds: Set<UUID>,
    val wishlistSelectedIds: Set<UUID>
)

data class ApiUpdateSpendingPeriodModel(
    val spendingPeriodTemplateId: UUID?,
    val savingRateTarget: Double?,
    val state: String?,
    val scheduleIncomeSelectedIds: Set<UUID>?,
    val scheduleExpenseSelectedIds: Set<UUID>?,
    val budgetsSelectedIds: Set<UUID>?,
    val wishlistSelectedIds: Set<UUID>?
)

fun mapApiCreateSpendingPeriodToSpendingPeriod(request: ApiCreateSpendingPeriodModel): CreateSpendingPeriodInput {
    return CreateSpendingPeriodInput(
        spendingPeriodTemplateId = request.spendingPeriodTemplateId,
        startDate = LocalDate.now(),
        endDate = LocalDate.now(),
        savingRateTarget = request.savingRateTarget,
        state = SpendingPeriodStateType.fromString(request.state),
        scheduleIncomesSelected = request.scheduleIncomeSelectedIds,
        scheduleExpensesSelected = request.scheduleExpenseSelectedIds,
        budgetsSelected = request.budgetsSelectedIds,
        wishlistItemSelected = request.wishlistSelectedIds
    )
}

fun mapApiUpdateSpendingPeriodToSpendingPeriod(id: UUID, request: ApiUpdateSpendingPeriodModel): UpdateSpendingPeriodInput {
    return UpdateSpendingPeriodInput(
        id = id,
        spendingPeriodTemplateId = request.spendingPeriodTemplateId,
        savingRateTarget = request.savingRateTarget,
        state = request.state?.let { SpendingPeriodStateType.fromString(it) },
        budgetsSelected = request.budgetsSelectedIds,
        scheduleExpensesSelected = request.scheduleExpenseSelectedIds,
        scheduleIncomesSelected = request.scheduleIncomeSelectedIds,
        wishlistItemSelected = request.wishlistSelectedIds,
    )
}
