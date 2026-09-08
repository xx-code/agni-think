package dev.auguste.agni_api.core.usecases.bank_registers.dto

import java.util.UUID

data class AccountLinkerOutput(
    val accountId: UUID? = null,
    val accountName: String,
    val bankRegisterId: String,
    val bankAccountName: String
)
data class GetBankRegisterOutput(
    val id: UUID,
    val institutionId: String,
    val title: String,
    val accessCode: String,
    val cursor: String,
    val isActive: Boolean,
    val accounts: List<AccountLinkerOutput>
)