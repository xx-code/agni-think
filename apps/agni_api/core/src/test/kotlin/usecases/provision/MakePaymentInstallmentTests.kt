package usecases.provision

import adapters.dto.RepoList
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.NotificationEventContent
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Fund
import domain.entities.Provision
import domain.enums.PeriodType
import domain.enums.ProvisionType
import domain.value_objects.ProvisionPayment
import domain.value_objects.Scheduler
import domain.value_objects.SchedulerRecurrence
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import usecases.funds.dto.DecreaseFundInput
import usecases.interfaces.IUseCase
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

class MakePaymentInstallmentTests {
    private val provisionRepo = mockk<IRepository<Provision>>()
    private val fundRepo = mockk<IRepository<Fund>>()
    private val decreaseFund = mockk<IUseCase<DecreaseFundInput, Unit>>()
    private val eventManager = mockk<IEventRegister>()
    private val unitOfWork = mockk<IUnitOfWork>()
    private val useCase = MakePaymentInstallment(provisionRepo, fundRepo, decreaseFund, eventManager, unitOfWork)

    @Test
    fun makes payment when due() = runBlocking {
        val fundId = UUID.randomUUID()
        val fund = Fund(id = fundId, userId = UUID.randomUUID(), title = "Fund", balance = 1000.0)
        val payment = ProvisionPayment(
            accountId = UUID.randomUUID(),
            categoryId = UUID.randomUUID(),
            budgetIds = emptySet(),
            tagIds = emptySet(),
            paymentAmount = 100.0,
            scheduler = Scheduler(LocalDateTime.now().minusDays(1), SchedulerRecurrence(PeriodType.MONTH, 1)),
            endDate = LocalDate.now().plusMonths(1)
        )
        val provision = Provision(
            id = UUID.randomUUID(),
            title = "Loan",
            userId = UUID.randomUUID(),
            type = ProvisionType.DEPRECIATE_LOAN,
            amount = 500.0,
            acquisitionDate = LocalDate.now().minusMonths(6),
            paymentInfo = payment,
            fundAmortizationId = fundId
        )

        every { provisionRepo.getAll(any(), any()) } returns RepoList(listOf(provision), 1)
        every { fundRepo.getManyByIds(any()) } returns listOf(fund)
        every { provisionRepo.update(any()) } returns Unit
        coEvery { decreaseFund.processDirect(any()) } returns Unit
        every { eventManager.notify(any(), any()) } returns Unit

        val result = useCase.execute(Unit).getOrThrow()
        assertEquals("Apply Provision Payment Installment Success", result.message)
        verify(exactly = 1) { provisionRepo.update(any()) }
    }
}
