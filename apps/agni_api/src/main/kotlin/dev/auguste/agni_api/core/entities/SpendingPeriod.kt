package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.value_objects.SpendingPeriodItem
import java.time.LocalDate
import java.util.UUID

class SpendingPeriod(
    id: UUID = UUID.randomUUID(),
    spendingPeriodTemplateId: UUID,
    startDate: LocalDate,
    endDate: LocalDate,
    suggestionAmount: Double,
    amount: Double,
    wantSpendingItems: List<SpendingPeriodItem>
): Entity(id) {
    var spendingPeriodTemplateId by cleanObservable(spendingPeriodTemplateId, this)
    var startDate by cleanObservable(startDate, this)
    var endDate by cleanObservable(endDate, this)
    var suggestionAmount by cleanObservable(suggestionAmount, this)
    var amount by cleanObservable(amount, this)
    var wantSpendingItems by cleanObservable(wantSpendingItems, this)
}