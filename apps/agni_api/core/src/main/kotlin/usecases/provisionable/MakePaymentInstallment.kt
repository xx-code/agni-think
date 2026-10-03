package usecases.provisionable

import usecases.interfaces.IUseCase

import adapters.dto.QueryFilter
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.NotificationEventContent
import adapters.events.contents.NotificationType
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Provision
import domain.entities.Fund
import usecases.dto.BackgroundTaskOut
import usecases.saving_goals.dto.DecreaseSavingGoalInput
import java.time.LocalDate
import kotlin.Throwable
import domain.enums.ProvisionType
import usecases.UseCase

class MakePaymentInstallment(
    private val provisionRepo: IRepository<Provision>,
    private val fundRepo: IRepository<Fund>,
    private val decreaseFund: IUseCase<DecreaseSavingGoalInput, Unit>,
    private val eventManager: IEventRegister,
    unitOfWork: IUnitOfWork,
): UseCase<Unit, BackgroundTaskOut>(unitOfWork) {
    override suspend fun process(input: Unit): BackgroundTaskOut {
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
                val payment = provision.paymentInfo ?: continue
                if (!payment.scheduler.isDueDate()) continue

                val fund = fundsById[provision.fundAmortizationId] ?: continue
                if (fund.balance < payment.paymentAmount) continue

                provision.paymentInfo = payment.copy(
                    scheduler = payment.scheduler.copy(
                        date = payment.scheduler.upgradeDate()
                    )
                )

                provisionRepo.update(provision)

                decreaseFund.processDirect(
                    DecreaseSavingGoalInput(
                        savingGoalId = fund.id,
                        accountId = payment.accountId,
                        amount = payment.paymentAmount
                    )
                )
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