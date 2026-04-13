package dev.auguste.agni_api.core.usecases.bank_registers.dto

import java.util.UUID

data class UpdateBankRegisterInput(
    val bankRegisterId: UUID,
    val title: String?,
    val accessCode: String?,
    val cursor: String?,
    val accounts: List<AccountLinkerInput>?
)