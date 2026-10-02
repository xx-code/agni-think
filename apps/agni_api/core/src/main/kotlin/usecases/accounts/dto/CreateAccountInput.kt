package usecases.accounts.dto

import domain.interfaces.IAccountDetail
import java.util.UUID

data class CreateAccountInput(
    val title: String,
    var initBalance: Double,
    val currencyId: UUID?,
    val color: String,
    val detail: IAccountDetail
)
