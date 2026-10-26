package usecases.bank_registers.dto

import java.util.UUID

data class SyncBankTransactionInput(
    val userId: UUID? = null
)
