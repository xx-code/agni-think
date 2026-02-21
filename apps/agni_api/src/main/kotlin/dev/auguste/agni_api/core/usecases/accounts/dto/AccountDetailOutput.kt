package dev.auguste.agni_api.core.usecases.accounts.dto

import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.enums.ContributionAccountType
import dev.auguste.agni_api.core.entities.enums.ManagementAccountType
import dev.auguste.agni_api.core.entities.interfaces.IAccountDetail
import dev.auguste.agni_api.core.entities.roundTo
import dev.auguste.agni_api.core.value_objects.BrokingAccountDetail
import dev.auguste.agni_api.core.value_objects.CheckingAccountDetail
import dev.auguste.agni_api.core.value_objects.CreditCardAccountDetail
import java.time.LocalDate
import kotlin.math.abs

data class GetBrokingDetailOutput(
    val managementType: ManagementAccountType,
    val contributionType: ContributionAccountType,
)

data class GetCreditCardAccountOutput(
    val creditCardLimit: Double,
    val creditUtilisation: Double,
    val nextInvoicePayment: LocalDate,
)

data class GetCheckingDetailOutput(
    val buffer: Double
)

data class AccountDetailOutput(
    val detailForCreditCard: GetCreditCardAccountOutput? = null,
    val detailForBroking: GetBrokingDetailOutput? = null,
    val detailForChecking: GetCheckingDetailOutput? = null
)

fun mapperAccountDetailOutput(accountDetail: IAccountDetail, balance: Double = 0.0): AccountDetailOutput {
    return when(accountDetail.getType()) {
        AccountType.CHECKING -> {
            val detail = (accountDetail as CheckingAccountDetail)
            AccountDetailOutput(detailForChecking = GetCheckingDetailOutput(
                buffer = detail.buffer
            )
            )
        }
        AccountType.BROKING -> {
            val detail = (accountDetail as BrokingAccountDetail)
            AccountDetailOutput(
                detailForBroking = GetBrokingDetailOutput(
                    managementType = detail.managementType,
                    contributionType = detail.contributionType
                )
            )
        }

        AccountType.CREDIT_CARD -> {
            val detail = (accountDetail as CreditCardAccountDetail)

            val now = LocalDate.now()
            var nextPaymentDate = detail.invoiceDate
            while (nextPaymentDate.isBefore(now)) {
                nextPaymentDate = nextPaymentDate.plusMonths(1)
            }

            val utilization = if (detail.creditLimit > 0) {
                ((abs(balance) / detail.creditLimit).roundTo(2)) * 100
            } else {
                0.0
            }

            AccountDetailOutput(detailForCreditCard = GetCreditCardAccountOutput(
                creditUtilisation = utilization,
                creditCardLimit = detail.creditLimit,
                nextInvoicePayment = nextPaymentDate
            ))
        }

        AccountType.BUSINESS -> {
            AccountDetailOutput()
        }
        AccountType.SAVING -> {
            AccountDetailOutput()
        }
    }
}