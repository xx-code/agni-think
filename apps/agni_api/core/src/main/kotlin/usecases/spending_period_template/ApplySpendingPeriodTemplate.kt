package usecases.spending_period_template

import adapters.dto.QueryFilter
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.NotificationEventContent
import adapters.events.contents.NotificationType
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Profile
import domain.entities.SpendingPeriod
import domain.entities.SpendingPeriodTemplate
import domain.enums.SpendingPeriodStateType
import usecases.BackgroundTaskOut
import usecases.interfaces.ISuspendableUseCase
import usecases.interfaces.IUseCase
import usecases.spending_period.dto.ForcastSpendingPeriodInput
import usecases.spending_period.dto.ForcastSpendingPeriodOutput
import domain.value_objects.Scheduler
import domain.value_objects.SchedulerRecurrence
import domain.value_objects.SnapshotForcastSpendingPeriod
import java.time.LocalDate

class ApplySpendingPeriodTemplate(
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
    private val forecastSpendingPeriod: IUseCase<ForcastSpendingPeriodInput, ForcastSpendingPeriodOutput>,
    private val profileRepo: IRepository<Profile>,
    private val unitOfWork: IUnitOfWork,
    private val eventRegister: IEventRegister,
): ISuspendableUseCase<Unit, BackgroundTaskOut> {
    override suspend fun execAsync(input: Unit): BackgroundTaskOut {
        try {
            val conditionSpendingPeriod = QueryExtendBuilder<SpendingPeriod>()
                .addCondition("state", QueryComparator.NotEqual, SpendingPeriodStateType.COMPLETE.value)
                .addCondition("endDate", QueryComparator.Lesser, LocalDate.now())
            val spendingPeriods = spendingPeriodRepo.getAll(QueryFilter.queryAll(), conditionSpendingPeriod)

            // spending period whose end date has passed becomes in progress
            for (spendingPeriod in spendingPeriods.items) {
                spendingPeriod.state = SpendingPeriodStateType.IN_PROGRESS
                spendingPeriodRepo.update(spendingPeriod)
            }

            val condition = QueryExtendBuilder<SpendingPeriodTemplate>()
                .addCondition("isActive", QueryComparator.Equal, true)
            val spendingPeriodTemps = spendingPeriodTemplateRepo.getAll(QueryFilter.queryAll(), condition)

            for (spendingPeriodTemp in spendingPeriodTemps.items.filter { it.checkIsActive() }) {
                if (spendingPeriodTemp.startDate <= LocalDate.now()) {
                    val scheduler = Scheduler(
                        date = spendingPeriodTemp.startDate.atStartOfDay(),
                        repeater = SchedulerRecurrence(
                            period = spendingPeriodTemp.recurrence.period,
                            interval = spendingPeriodTemp.recurrence.interval
                        )
                    )

                    unitOfWork.execute {
                        val updateDate = scheduler.upgradeDate().toLocalDate()

                        val profiles = profileRepo.getAll(QueryFilter.queryAll())
                        val savingRate = profiles.items.firstOrNull()?.savingPercentage ?: 0.0

                        val forecastRes = forecastSpendingPeriod.execAsync(
                            input = ForcastSpendingPeriodInput(
                                startDate = spendingPeriodTemp.startDate,
                                endDate = updateDate,
                                budgetIds = spendingPeriodTemp.targetBudgetIds.toList(),
                                savingRate = savingRate,
                            )
                        )


                        spendingPeriodRepo.create(SpendingPeriod(
                            spendingPeriodTemplateId = spendingPeriodTemp.id,
                            startDate = spendingPeriodTemp.startDate,
                            endDate = updateDate,
                            freeAmount = forecastRes.expectedRemainAmount,
                            savingRateTarget = savingRate,
                            totalExpectedIncome = forecastRes.totalExpectedIncome,
                            totalExpectedExpenses = forecastRes.totalExpectedExpense,
                            state = SpendingPeriodStateType.DRAFT,
                            wantSpendingItems = listOf(),
                            snapshot = SnapshotForcastSpendingPeriod(
                                income = 0.0,
                                fixExpenses = 0.0,
                                variableExpenses = 0.0,
                                budgetExpenses = 0.0,
                                saving = 0.0
                            )
                        ))

                        spendingPeriodTemp.startDate = updateDate
                        spendingPeriodTemplateRepo.update(spendingPeriodTemp)
                    }


                    eventRegister.notify(
                        EventType.NOTIFICATION, NotificationEventContent(
                            "Periode de depense",
                            "Debut de period de depense de ${scheduler.date} - ${scheduler.upgradeDate()}",
                            type = NotificationType.Success
                        )
                    )
                }
            }

            return BackgroundTaskOut("Spending Period Template Complete")
        } catch (error: Throwable) {
            this.eventRegister.notify(
                EventType.NOTIFICATION, NotificationEventContent(
                    "Spending Period Factory Error",
                    "Error while applying schedule in voice ${error.message}",
                    type = NotificationType.Error,
                )
            )

            return BackgroundTaskOut(error.localizedMessage)
        }
    }
}