package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.enums.ContributionAccountType
import dev.auguste.agni_api.core.entities.enums.ManagementAccountType
import dev.auguste.agni_api.core.entities.interfaces.IAccountDetail
import dev.auguste.agni_api.core.usecases.accounts.dto.CreateAccountInput
import dev.auguste.agni_api.core.usecases.accounts.dto.UpdateAccountInput
import dev.auguste.agni_api.core.value_objects.BrokingAccountDetail
import dev.auguste.agni_api.core.value_objects.CheckingAccountDetail
import dev.auguste.agni_api.core.value_objects.CreditCardAccountDetail
import dev.auguste.agni_api.core.value_objects.SavingAccountDetail
import jakarta.validation.constraints.Min
import org.apache.coyote.BadRequestException
import java.util.UUID


data class ApiAccountDetailModel(
    @field:Min(0, message = "Credit limit must be positive")
    val creditLimit: Double?,

    val contributionType: ContributionAccountType?,
    val managementAccountType: ManagementAccountType?,

    @field:Min(value = 0, message = "Buffer must be positive")
    val buffer: Double?,

    @field:Min(value = 0, message = "Secure amount must be positive")
    val secureAmount: Double?
)

data class ApiCreateAccountModel(
    val title: String,
    val type: AccountType,
    val currencyId: UUID?,
    val detail: ApiAccountDetailModel
)

data class ApiUpdateAccountModel(
    val title: String? = null,
    val type: AccountType? = null,
    val currencyId: UUID? = null,
    val detail: ApiAccountDetailModel? = null,
)

fun mapAccountDetail(type: AccountType, model: ApiAccountDetailModel) : IAccountDetail {
    return when(type) {
        AccountType.CHECKING -> {
            if (model.buffer == null || model.buffer < 0)
                throw BadRequestException("Buffer must define. or not negative")
            CheckingAccountDetail(model.buffer)
        }

        AccountType.CREDIT_CARD -> {
            if (model.creditLimit == null)
                throw BadRequestException("Buffer must define. or not negative")

            CreditCardAccountDetail(creditLimit = model.creditLimit)
        }

        AccountType.SAVING -> {
            if (model.secureAmount == null)
                throw BadRequestException("Buffer must define. or not negative")

            SavingAccountDetail(secureAmount = model.secureAmount)
        }

        AccountType.BROKING -> {
            if (model.managementAccountType == null || model.contributionType == null)
                throw BadRequestException("Management account et contribution type type must define.")
            BrokingAccountDetail(model.managementAccountType, model.contributionType)
        }
        else -> CheckingAccountDetail(0.0)
    }
}

fun mapApiUpdateModel(id: UUID, model: ApiUpdateAccountModel): UpdateAccountInput {
    return UpdateAccountInput(
        id = id,
        title = model.title ,
        detail = if (model.type != null && model.detail != null)
            mapAccountDetail(model.type, model.detail)
        else null
    )
}

fun mapApiCreateAccountModel(model: ApiCreateAccountModel): CreateAccountInput {
    return CreateAccountInput(
        title = model.title,
        initBalance = 0.0,
        currencyId = model.currencyId,
        detail = mapAccountDetail(model.type, model.detail)
    )
}
