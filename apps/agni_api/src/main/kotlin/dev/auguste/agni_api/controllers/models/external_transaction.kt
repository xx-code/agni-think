package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.usecases.invoices.dto.AddExternalTransactionInput
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

data class ApiAppExternalTransactionModel(
    val transactionId: String,
    val accountId: String,
    val amount: Double,
    @field:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val dateTransaction: LocalDateTime,
    val merchantName: String,
    val categoryPrimary: String,
    val categoryDetail: String,
    val isTreated: Boolean = false
)

fun mapApiExternalTransactionModel(apiTransaction: ApiAppExternalTransactionModel): AddExternalTransactionInput {
    return AddExternalTransactionInput(
        accountId = apiTransaction.accountId,
        transactionId = apiTransaction.transactionId,
        amount = apiTransaction.amount,
        dateTransaction = apiTransaction.dateTransaction,
        merchantName = apiTransaction.merchantName,
        categoryPrimary = apiTransaction.categoryPrimary,
        categoryDetail = apiTransaction.categoryDetail,
        isTreated = apiTransaction.isTreated
    )
}
