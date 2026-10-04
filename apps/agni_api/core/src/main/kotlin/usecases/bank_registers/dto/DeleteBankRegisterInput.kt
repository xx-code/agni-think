package usecases.bank_registers.dto

import java.util.UUID

data class DeleteBankRegisterInput(
    val bankRegisterId: UUID
)