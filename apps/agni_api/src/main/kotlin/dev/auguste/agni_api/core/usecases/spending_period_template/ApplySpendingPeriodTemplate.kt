package dev.auguste.agni_api.core.usecases.spending_period_template

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.events.EventType
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.events.contents.NotificationEventContent
import dev.auguste.agni_api.core.adapters.events.contents.NotificationType
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.adapters.repositories.QueryExtendBuilder
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.entities.SpendingPeriod
import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.entities.enums.SpendingPeriodStateType
import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.analystics.dto.ForcastSpendingInput
import dev.auguste.agni_api.core.usecases.analystics.dto.ForcastSpendingOutput
import dev.auguste.agni_api.core.usecases.interfaces.ISuspendableUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.value_objects.Scheduler
import dev.auguste.agni_api.core.value_objects.SchedulerRecurrence
import java.time.LocalDate

class ApplySpendingPeriodTemplate(
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
    private val forecastSpendingPeriod: IUseCase<ForcastSpendingInput, ForcastSpendingOutput>,
    private val unitOfWork: IUnitOfWork,
    private val eventRegister: IEventRegister,
): ISuspendableUseCase<Unit, BackgroundTaskOut> {
    override suspend fun execAsync(input: Unit): BackgroundTaskOut {
        try {
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

                        val forecastRes = forecastSpendingPeriod.execAsync(
                            input = ForcastSpendingInput(
                                startDate = spendingPeriodTemp.startDate,
                                endDate = updateDate,
                                wantItems = listOf(),
                                savingAdditionalIncome = listOf(),
                                budgetIds = listOf()
                            )
                        )

                        spendingPeriodRepo.create(SpendingPeriod(
                            spendingPeriodTemplateId = spendingPeriodTemp.id,
                            startDate = spendingPeriodTemp.startDate,
                            endDate = updateDate,
                            suggestionAmount = forecastRes.remainAmount,
                            savingsTarget = forecastRes.expectedSaving,
                            totalExpectedIncome = forecastRes.totalExpectedIncome,
                            totalExpectedExpenses = forecastRes.totalExpectedExpense,
                            state = SpendingPeriodStateType.PENDING,
                            wantSpendingItems = listOf()
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