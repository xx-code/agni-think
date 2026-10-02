package usecases.provisionable

import adapters.dto.QueryFilter
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.NotificationEventContent
import adapters.events.contents.NotificationType
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.query_extend.QueryComparator
import domain.entities.Provision
import domain.entities.Fund
import usecases.BackgroundTaskOut
import usecases.interfaces.IInnerUseCase
import usecases.interfaces.ISuspendableUseCase
import usecases.saving_goals.dto.DecreaseSavingGoalInput
import java.time.LocalDate
import kotlin.Throwable

class MakePaymentInstallment(
    private val provisionRepo: IRepository<Provision>,
    private val fundRepo: IRepository<Fund>,
    private val decreaseFund:  IInnerUseCase<DecreaseSavingGoalInput, Unit>,
    private val eventManager: IEventRegister,
    private val unitOfWork: IUnitOfWork,
): ISuspendableUseCase<Unit, BackgroundTaskOut> {
    override suspend fun execAsync(input: Unit): BackgroundTaskOut {
        try {
            val condition = QueryExtendBuilder<Provision>()
                .addCondition("type", QueryComparator.Equal, _root_ide_package_.domain.enums.ProvisionType.DEPRECIATE_LOAN.value)
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