package dev.auguste.agni_api.core.usecases.bank_registers.dto

import java.util.UUID

data class DeleteBankRegisterInput(
    val bankRegisterId: UUID
)