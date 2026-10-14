package usecases.schedule_Invoices

import adapters.dto.QueryFilter
import adapters.dto.RepoList
import adapters.events.IEventRegister
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.ScheduleInvoice
import domain.enums.InvoiceType
import domain.enums.PeriodType
import domain.value_objects.Scheduler
import domain.value_objects.SchedulerRecurrence
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import usecases.dto.CreatedOutput
import usecases.invoices.dto.CreateFreezeInvoiceInput
import usecases.invoices.dto.CreateInvoiceInput
import usecases.interfaces.IUseCase
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

class ApplyScheduleInvoiceTests {
    private val scheduleInvoiceRepo = mockk<IRepository<ScheduleInvoice>>()
    private val createInvoice = mockk<IUseCase<CreateInvoiceInput, CreatedOutput>>()
    private val createFreezeInvoice = mockk<IUseCase<CreateFreezeInvoiceInput, CreatedOutput>>()
    private val eventManager = mockk<IEventRegister>()
    private val unitOfWork = mockk<IUnitOfWork>()
    private val useCase = ApplyScheduleInvoice(
        scheduleInvoiceRepo,
        createInvoice,
        createFreezeInvoice,
        eventManager,
        unitOfWork
    )

    @Test
    fun creates invoice when schedule is due() = runBlocking {
        val now = LocalDateTime.now()
        val schedule = ScheduleInvoice(
            title = "Bill",
            accountId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            categoryId = UUID.randomUUID(),
            amount = 100.0,
            type = InvoiceType.VARIABLE_COST,
            scheduler = Scheduler(now.minusDays(1), SchedulerRecurrence(PeriodType.MONTH, 1)),
            endDate = now.plusMonths(1)
        )

        coEvery { createInvoice.processDirect(any()) } returns CreatedOutput(UUID.randomUUID())
        coEvery { createFreezeInvoice.processDirect(any()) } returns CreatedOutput(UUID.randomUUID())
        every { scheduleInvoiceRepo.update(any()) } returns Unit
        every { eventManager.notify(any(), any()) } returns Unit
        every { scheduleInvoiceRepo.getAll(any(), any()) } returns RepoList(listOf(schedule), 1)

        val result = useCase.execute(Unit).getOrThrow()
        assertEquals("Apply Schedule Success", result.message)

        coVerify(exactly = 1) { createInvoice.processDirect(any()) }
        coVerify(exactly = 0) { createFreezeInvoice.processDirect(any()) }
    }
}
