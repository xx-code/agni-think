package dev.auguste.agni_api.core.usecases.bank_registers.dto

import java.util.UUID

data class AccountLinkerOutput(
    val accountId: UUID,
    val accountName: String,
    val bankRegisterId: String,
)
data class GetBankRegisterOutput(
    val id: UUID,
    val title: String,
    val accessCode: String,
    val cursor: String,
    val isActive: Boolean,
    val accounts: List<AccountLinkerOutput>
)