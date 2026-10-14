package usecases.bank_registers.dto

import adapters.dto.QueryFilter

data class GetAllBankRegisterInput(
    val queryFilter: QueryFilter
)
