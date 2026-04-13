package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.usecases.bank_registers.dto.AccountLinkerInput
import dev.auguste.agni_api.core.usecases.bank_registers.dto.AccountLinkerOutput
import dev.auguste.agni_api.core.usecases.bank_registers.dto.CreateBankRegisterInput
import dev.auguste.agni_api.core.usecases.bank_registers.dto.GetBankRegisterOutput
import dev.auguste.agni_api.core.usecases.bank_registers.dto.UpdateBankRegisterInput
import jakarta.validation.constraints.NotEmpty
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle
import java.util.UUID

data class ApiAccountLinkerModel(
    val accountId: UUID,
    val bankAccountId: String
)

data class ApiCreateBankRegisterModel(
    @field:NotEmpty("Access Code must not be empty")
    val accessCode: String,
    @field:NotEmpty("Title must not be empty")
    val title: String,
    val accounts: List<ApiAccountLinkerModel>
)

data class ApiUpdateBankRegisterModel(
    val title: String?=null,
    val accessCode: String? = null,
    val cursor: String? = null,
    val accounts: List<ApiAccountLinkerModel>? = null
)

data class ApiSecureBankRegisterOutput(
    val title: String,
    val bankRegisterId: UUID,
    val isActive: Boolean,
    val accounts: List<AccountLinkerOutput>
)

fun mapBankRegisterToSecure(bankRegister: GetBankRegisterOutput): ApiSecureBankRegisterOutput {
    return ApiSecureBankRegisterOutput(
        bankRegisterId = bankRegister.id,
        title = bankRegister.title,
        isActive = bankRegister.isActive,
        accounts = bankRegister.accounts
    )
}

fun mapApiCreateBankRegister(model: ApiCreateBankRegisterModel): CreateBankRegisterInput{
    return CreateBankRegisterInput(
        accessCode = model.accessCode,
        title = model.title,
        accounts = model.accounts.map { AccountLinkerInput(it.accountId, it.bankAccountId) }
    )
}

fun mapApiUpdateBankRegister(id: UUID, model: ApiUpdateBankRegisterModel): UpdateBankRegisterInput {
    return UpdateBankRegisterInput(
        bankRegisterId = id,
        title = model.title,
        accessCode = model.accessCode,
        cursor = model.cursor,
        accounts = model.accounts?.map { AccountLinkerInput(it.accountId, it.bankAccountId) }
    )
}