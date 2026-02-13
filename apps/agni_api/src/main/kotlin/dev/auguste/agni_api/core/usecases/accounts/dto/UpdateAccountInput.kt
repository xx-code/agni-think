package dev.auguste.agni_api.core.usecases.accounts.dto

import dev.auguste.agni_api.core.entities.interfaces.IAccountDetail
import java.util.UUID

data class UpdateAccountInput(
    val id: UUID,
    val title: String?,
    val detail: IAccountDetail?
)
