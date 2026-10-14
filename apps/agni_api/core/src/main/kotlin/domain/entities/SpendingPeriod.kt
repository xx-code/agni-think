package domain.entities

import domain.enums.SpendingPeriodStateType
import domain.exceptions.ValidationException
import domain.value_objects.SnapshotForcastSpendingPeriod
import domain.value_objects.SpendingPeriodItem
import java.time.LocalDate
import java.util.UUID

class SpendingPeriod(
    id: UUID = UUID.randomUUID(),
    val userId: UUID,
    spendingPeriodTemplateId: UUID,
    startDate: LocalDate,
    endDate: LocalDate,
    savingRateTarget: Double,
    scheduleIncomesSelected: Set<UUID>,
    scheduleExpensesSelected: Set<UUID>,
    budgetsSelected: Set<UUID>,
    closeBalance: Double = 0.0,
    wishlistItemSelected: Set<UUID>,
    state: SpendingPeriodStateType,
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
    var savingRateTarget by cleanObservable(savingRateTarget, this)
    var state by cleanObservable(state, this)

    var scheduleIncomesSelected by cleanObservable(scheduleIncomesSelected, this)
    var scheduleExpensesSelected by cleanObservable(scheduleExpensesSelected, this)
    var budgetsSelected by cleanObservable(budgetsSelected, this)
    var wishlistItemSelected by cleanObservable(wishlistItemSelected, this)
    var snapshot by cleanObservable(snapshot, this)
    var closedBalance by cleanObservable(closeBalance, this)
}