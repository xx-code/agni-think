package adapters.dto

import java.time.LocalDateTime
import java.util.UUID

data class AccountSnapshotBalanceOutput(
    val id: UUID,
    val accountId: UUID,
    val balance: Double,
    val date: LocalDateTime
)
