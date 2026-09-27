package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.entities.enums.SpendingPeriodStateType
import dev.auguste.agni_api.core.value_objects.SnapshotForcastSpendingPeriod
import dev.auguste.agni_api.core.value_objects.SpendingPeriodItem
import java.time.LocalDate
import java.util.UUID

class SpendingPeriod(
    id: UUID = UUID.randomUUID(),
    spendingPeriodTemplateId: UUID,
    startDate: LocalDate,
    endDate: LocalDate,
    closeBalance: Double = 0.0,
    freeAmount: Double,
    savingRateTarget: Double,
    totalExpectedIncome: Double,
    totalExpectedExpenses: Double,
    state: SpendingPeriodStateType,
    wantSpendingItems: List<SpendingPeriodItem>,
    snapshot: SnapshotForcastSpendingPeriod
): Entity(id) {
    var spendingPeriodTemplateId by cleanObservable(spendingPeriodTemplateId, this)
    var startDate by cleanObservable(startDate, this)
    var endDate by cleanObservable(endDate, this)
    var freeAmount by cleanObservable(freeAmount, this)
    var savingRateTarget by cleanObservable(savingRateTarget, this)
    var totalExpectedIncome by cleanObservable(totalExpectedIncome, this)
    var totalExpectedExpenses by cleanObservable(totalExpectedExpenses, this)
    var state by cleanObservable(state, this)
    var wantSpendingItems by cleanObservable(wantSpendingItems, this)
    var snapshot by cleanObservable(snapshot, this)
    var closedBalance by cleanObservable(closeBalance, this)
}