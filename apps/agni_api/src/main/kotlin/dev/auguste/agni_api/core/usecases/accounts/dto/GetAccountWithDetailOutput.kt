package dev.auguste.agni_api.core.usecases.accounts.dto

import java.util.UUID

data class GetAccountWithDetailOutput(
    val id: UUID,
    val title: String,
    val balance: Double,
    val type: String,
    val lockedBalance: Double,
    val freezeBalance: Double,
    val color: String,
    val detail: AccountDetailOutput
)
