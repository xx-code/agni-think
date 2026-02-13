package dev.auguste.agni_api.core.usecases.accounts.dto

import dev.auguste.agni_api.core.entities.enums.AccountType
import java.util.UUID

data class GetAccountWithDetailOutput(
    val id: UUID,
    val title: String,
    val balance: Double,
    val type: String,
    val lockedBalance: Double,
    val freezeBalance: Double,
    val detail: AccountDetailOutput
)
