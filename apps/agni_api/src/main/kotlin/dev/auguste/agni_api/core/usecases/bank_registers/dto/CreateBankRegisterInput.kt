package dev.auguste.agni_api.core.usecases.bank_registers.dto

import java.util.UUID

data class AccountLinkerInput(
    val accountId: UUID?,
    val bankName: String,
    val bankAccountId: String,
)

data class CreateBankRegisterInput(
    val title: String,
    val institutionId: String,
    val accessCode: String,
    val accounts: List<AccountLinkerInput>
)