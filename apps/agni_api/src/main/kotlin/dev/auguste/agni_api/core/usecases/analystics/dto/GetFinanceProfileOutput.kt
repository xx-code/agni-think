package dev.auguste.agni_api.core.usecases.analystics.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class AccountInfoOutput(
    val accountName: String,
    val accountType: String,
    val balance: Double,
    val accountDetailRule: String,
    val isLiquidity: Boolean
)

data class PrincipeToFollowOutput(
    val title: String,
    val ruleToFollow: String
)

data class ComingSpendingOutput(
    val title: String,
    val amount: Double,
    val dueDate: LocalDateTime
)

data class ComingRevenueOutput(
    val title: String,
    val amount: Double,
    val dueDate: LocalDateTime
)

data class IncomeSourceOutput(
    val title: String,
    val type: String,
    val grossAmount: Double?,
    val estimateNextNetAmount: Double,
    val estimatedFutureOccurrences: Int,
    val confidence: Int,
)

data class GetFinanceProfileOutput (
    val accountInfos: List<AccountInfoOutput>,
    val principles: List<PrincipeToFollowOutput>,
    val comingSpending: List<ComingSpendingOutput>,
    val comingRevenue: List<ComingRevenueOutput>,
    val incomeSources: List<IncomeSourceOutput>
)