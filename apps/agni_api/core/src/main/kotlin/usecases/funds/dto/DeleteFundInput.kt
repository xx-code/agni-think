package usecases.funds.dto

import java.util.UUID

data class DeleteFundInput (val fundId: UUID, val accountId: UUID?)