package dev.auguste.agni_api.core.usecases.analystics.dto

import java.time.LocalDate
import java.util.UUID

data class SavingAdditionalIncomeInput(
    val savingAccountId: UUID,
    val amount: Double
)

data class ForcastSpendingInput(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val wantItems: List<WantItemOutput>,
    val savingAdditionalIncome: List<SavingAdditionalIncomeInput>,
    val overrideAccountsBalance: Double? = null,
    val savingRate: Double? = null,
)