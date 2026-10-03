package usecases.accounts.dto

import domain.interfaces.IAccountDetail
import domain.roundTo
import domain.value_objects.BrokingAccountDetail
import domain.value_objects.CheckingAccountDetail
import domain.value_objects.CreditCardAccountDetail
import java.time.LocalDate
import kotlin.math.abs
import domain.enums.AccountType
import domain.enums.ContributionAccountType
import domain.enums.ManagementAccountType

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