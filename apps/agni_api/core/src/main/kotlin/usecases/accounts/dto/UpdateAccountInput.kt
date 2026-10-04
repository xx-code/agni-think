package usecases.accounts.dto

import domain.interfaces.IAccountDetail
import java.util.UUID

data class UpdateAccountInput(
    val id: UUID,
    val title: String?,
    val detail: IAccountDetail?,
    val color: String?
)
