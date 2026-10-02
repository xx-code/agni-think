package domain.entities

import domain.enums.SpendingPeriodStateType
import domain.exceptions.ValidationException
import domain.value_objects.SnapshotForcastSpendingPeriod
import domain.value_objects.SpendingPeriodItem
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
    var startDate: LocalDate by cleanObservable(startDate, this, {
        it < this.endDate
    }) {
        ValidationException.SpendingPeriodStartDateMustBeLesserThanEndDate(it, this.endDate)
    }
    var endDate: LocalDate by cleanObservable(endDate, this, {
        it > this.startDate
    }) {
        ValidationException.SpendingPeriodEndDateMustBeGreaterThanStartDate(this.startDate, it)
    }
    var freeAmount by cleanObservable(freeAmount, this)
    var savingRateTarget by cleanObservable(savingRateTarget, this)
    var totalExpectedIncome by cleanObservable(totalExpectedIncome, this)
    var totalExpectedExpenses by cleanObservable(totalExpectedExpenses, this)
    var state by cleanObservable(state, this)
    var wantSpendingItems by cleanObservable(wantSpendingItems, this)
    var snapshot by cleanObservable(snapshot, this)
    var closedBalance by cleanObservable(closeBalance, this)
}