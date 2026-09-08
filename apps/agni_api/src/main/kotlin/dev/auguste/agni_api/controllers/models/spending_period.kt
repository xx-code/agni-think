package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.entities.enums.SpendingPeriodStateType
import dev.auguste.agni_api.core.usecases.spending_period.dto.CreateSpendingPeriodInput
import dev.auguste.agni_api.core.usecases.spending_period.dto.SpendingPeriodItemInput
import dev.auguste.agni_api.core.usecases.spending_period.dto.UpdateSpendingPeriodInput
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.util.UUID

data class ApiSpendingPeriodItemModel(
    @field:NotBlank("Item description must be defined")
    val description: String,
    @field:NotNull("Item amount must be defined")
    @field:Min(0, "Item amount must be greater than or equal to 0")
    val amount: Double
)

data class ApiCreateSpendingPeriodModel(
    @field:NotNull("Spending period template id must be defined")
    val spendingPeriodTemplateId: UUID,
    @field:NotNull("Suggestion amount must be defined")
    @field:Min(0, "Suggestion amount must be greater than or equal to 0")
    val suggestionAmount: Double,
    @field:NotNull("Savings target must be defined")
    @field:Min(0, "Savings target must be greater than or equal to 0")
    val savingsTarget: Double,
    @field:NotNull("Total expected income must be defined")
    @field:Min(0, "Total expected income must be greater than or equal to 0")
    val totalExpectedIncome: Double,
    @field:NotNull("Total expected expenses must be defined")
    @field:Min(0, "Total expected expenses must be greater than or equal to 0")
    val totalExpectedExpenses: Double,
    @field:NotNull("State must be defined")
    val state: String,
    @field:Valid
    val wantSpendingItems: List<ApiSpendingPeriodItemModel>
)

data class ApiUpdateSpendingPeriodModel(
    val spendingPeriodTemplateId: UUID?,
    val suggestionAmount: Double?,
    val savingsTarget: Double?,
    val totalExpectedIncome: Double?,
    val totalExpectedExpenses: Double?,
    val state: String?,
    @field:Valid
    val wantSpendingItems: List<ApiSpendingPeriodItemModel>?
)

fun mapApiCreateSpendingPeriodToSpendingPeriod(request: ApiCreateSpendingPeriodModel): CreateSpendingPeriodInput {
    return CreateSpendingPeriodInput(
        spendingPeriodTemplateId = request.spendingPeriodTemplateId,
        startDate = LocalDate.now(),
        endDate = LocalDate.now(),
        suggestionAmount = request.suggestionAmount,
        savingsTarget = request.savingsTarget,
        totalExpectedIncome = request.totalExpectedIncome,
        totalExpectedExpenses = request.totalExpectedExpenses,
        state = SpendingPeriodStateType.fromString(request.state),
        wantSpendingItems = request.wantSpendingItems.map {
            SpendingPeriodItemInput(
                description = it.description,
                amount = it.amount
            )
        }
    )
}

fun mapApiUpdateSpendingPeriodToSpendingPeriod(id: UUID, request: ApiUpdateSpendingPeriodModel): UpdateSpendingPeriodInput {
    return UpdateSpendingPeriodInput(
        id = id,
        spendingPeriodTemplateId = request.spendingPeriodTemplateId,
        suggestionAmount = request.suggestionAmount,
        savingsTarget = request.savingsTarget,
        state = request.state?.let { SpendingPeriodStateType.fromString(it) },
        totalExpectedIncome = request.totalExpectedIncome,
        totalExpectedExpenses = request.totalExpectedExpenses,
        wantSpendingItems = request.wantSpendingItems?.map {
            SpendingPeriodItemInput(
                description = it.description,
                amount = it.amount
            )
        }
    )
}
