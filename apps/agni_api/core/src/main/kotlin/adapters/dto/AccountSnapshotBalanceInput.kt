package adapters.dto

import java.time.LocalDateTime
import java.util.UUID

data class AccountSnapshotBalanceInput(
    val accountId: UUID,
    val balance: Double,
    val date: LocalDateTime
)
