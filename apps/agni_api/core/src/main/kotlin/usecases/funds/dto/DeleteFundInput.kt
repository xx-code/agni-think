package usecases.funds.dto

import java.util.UUID

data class DeleteFundInput (val savingGoalId: UUID, val accountId: UUID?)