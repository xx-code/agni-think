package domain.entities

import domain.enums.DepreciationType
import domain.enums.PeriodType
import domain.value_objects.ProvisionDepreciateCriteria
import domain.value_objects.ProvisionPayment
import domain.value_objects.Scheduler
import domain.value_objects.SchedulerRecurrence
import domain.value_objects.SnapshotForcastSpendingPeriod
import domain.value_objects.SpendingPeriodItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal fun monthlyScheduler(date: LocalDateTime = LocalDateTime.of(2026, 1, 5, 8, 30)): Scheduler =
    Scheduler(date, SchedulerRecurrence(PeriodType.MONTH, 1))

internal val spendingPeriodSnapshot = SnapshotForcastSpendingPeriod(
    income = 3_000.0,
    fixExpenses = 900.0,
    variableExpenses = 400.0,
    budgetExpenses = 200.0,
    saving = 1_500.0,
)

internal fun straightLineCriteria(value: Double = 10.0): ProvisionDepreciateCriteria =
    ProvisionDepreciateCriteria(
        title = "Linear",
        description = "Straight line depreciation",
        type = DepreciationType.STRAIGHT_LINE,
        value = value,
        monthRange = 0,
    )

internal fun provisionPayment(): ProvisionPayment = ProvisionPayment(
    accountId = UUID.randomUUID(),
    categoryId = UUID.randomUUID(),
    budgetIds = emptySet(),
    tagIds = emptySet(),
    paymentAmount = 120.0,
    scheduler = monthlyScheduler(),
    endDate = LocalDate.of(2026, 12, 31),
)

internal fun spendingItem(description: String = "Groceries", amount: Double = 75.0): SpendingPeriodItem =
    SpendingPeriodItem(description, amount)