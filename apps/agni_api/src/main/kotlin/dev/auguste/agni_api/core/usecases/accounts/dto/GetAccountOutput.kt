package dev.auguste.agni_api.core.usecases.accounts.dto

import java.util.UUID

data class GetAccountOutput(
    val id: UUID,
    val title: String,
    val balance: Double,
    val type: String,
    val color: String
)
