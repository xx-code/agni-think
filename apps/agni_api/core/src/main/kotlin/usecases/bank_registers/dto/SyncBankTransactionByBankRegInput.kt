package usecases.bank_registers.dto

import java.util.UUID

data class SyncBankTransactionByBankRegInput(
    val userId: UUID,
    val bankRegisterId: UUID
)
