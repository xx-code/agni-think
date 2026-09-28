package dev.auguste.agni_api.core.usecases.provisionable

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.events.EventType
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.events.contents.NotificationEventContent
import dev.auguste.agni_api.core.adapters.events.contents.NotificationType
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.adapters.repositories.QueryExtendBuilder
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.enums.ProvisionType
import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.ISuspendableUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.saving_goals.dto.DecreaseSavingGoalInput
import dev.auguste.agni_api.core.value_objects.ProvisionPayment
import dev.auguste.agni_api.core.value_objects.Scheduler
import java.time.LocalDate
import kotlin.Throwable

class MakePaymentInstallment(
    private val provisionRepo: IRepository<Provision>,
    private val fundRepo: IRepository<SavingGoal>,
    private val decreaseFund:  IInnerUseCase<DecreaseSavingGoalInput, Unit>,
    private val eventManager: IEventRegister,
    private val unitOfWork: IUnitOfWork,
): ISuspendableUseCase<Unit, BackgroundTaskOut> {
    override suspend fun execAsync(input: Unit): BackgroundTaskOut {
        try {
            val condition = QueryExtendBuilder<Provision>()
                .addCondition("type", QueryComparator.Equal, ProvisionType.DEPRECIATE_LOAN.value)
                .addCondition("paymentInfo.endDate", QueryComparator.GreaterOrEquals, LocalDate.now())
            val provisions = provisionRepo.getAll(QueryFilter.queryAll(), condition)
            val amortizedProvisions = provisions.items.filter { it.isAmortize() }
            val amortizedProvisionIds = amortizedProvisions.mapNotNull { it.fundAmortizationId }
            val funds = fundRepo.getManyByIds(amortizedProvisionIds.toSet())
            val fundsById = funds.associateBy { it.id }

            for (provision in amortizedProvisions) {
                unitOfWork.execute {
                    val payment = provision.paymentInfo ?: return@execute
                    if (!payment.scheduler.isDueDate()) return@execute

                    val fund = fundsById[provision.fundAmortizationId] ?: return@execute
                    if (fund.balance < payment.paymentAmount) return@execute

                    provision.paymentInfo = payment.copy(
                        scheduler = payment.scheduler.copy(
                            date = payment.scheduler.upgradeDate()
                        )
                    )

                    provisionRepo.update(provision)

                    decreaseFund.execInnerAsync(
                        DecreaseSavingGoalInput(
                            savingGoalId = fund.id,
                            accountId = payment.accountId,
                            amount = payment.paymentAmount
                        )
                    )
                }
            }

            return BackgroundTaskOut("Apply Provision Payment Installment Success")
        } catch (error: Throwable) {
            this.eventManager.notify(EventType.NOTIFICATION, NotificationEventContent(
                "Provision !Error",
                "Error while Make installment provision ${error.message}",
                type = NotificationType.Error,
            ))

            return BackgroundTaskOut(error.localizedMessage)
        }
    }
}