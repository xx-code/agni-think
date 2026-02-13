package dev.auguste.agni_api.core.usecases.accounts.dto

import dev.auguste.agni_api.core.entities.interfaces.IAccountDetail
import java.util.UUID

data class CreateAccountInput(
    val title: String,
    var initBalance: Double,
    val currencyId: UUID?,
    val detail: IAccountDetail
)
