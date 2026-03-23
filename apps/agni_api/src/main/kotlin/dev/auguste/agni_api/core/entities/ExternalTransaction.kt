package dev.auguste.agni_api.core.entities

import java.time.LocalDateTime
import java.util.UUID

class ExternalTransaction(
    id: UUID = UUID.randomUUID(),
    accountId: String,
    amount: Double,
    dateTransaction: LocalDateTime,
    merchantName: String,
    categoryPrimary: String,
    categoryDetail: String,
    isTreated: Boolean,
): Entity(id) {
    var accountId by cleanObservable(accountId, this)
    var amount by cleanObservable(amount, this)
    var dateTransaction by cleanObservable(dateTransaction, this)
    var merchantName by cleanObservable(merchantName, this)
    var categoryPrimary by cleanObservable(categoryPrimary, this)
    var categoryDetail by cleanObservable(categoryDetail, this)
    var isTreated by cleanObservable(isTreated, this)
}