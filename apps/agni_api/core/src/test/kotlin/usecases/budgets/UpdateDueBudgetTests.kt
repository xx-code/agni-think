package usecases.budgets

import adapters.dto.RepoList
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.NotificationEventContent
import adapters.repositories.IRepository
import domain.entities.Budget
import domain.enums.PeriodType
import domain.value_objects.Scheduler
import domain.value_objects.SchedulerRecurrence
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

class UpdateDueBudgetTests {
    private val budgetRepo = mockk<IRepository<Budget>>()
    private val eventRegister = mockk<IEventRegister>()
    private val useCase = UpdateDueBudget(budgetRepo, eventRegister)

    @Test
    fun updates due budget with repeater() = runBlocking {
        val now = LocalDateTime.now()
        val budget = Budget(
            id = UUID.randomUUID(),
            title = "Food",
            userId = UUID.randomUUID(),
            accountId = UUID.randomUUID(),
            targetAmount = 200.0,
            currentAmount = 50.0,
            startDate = now.minusMonths(2),
            endDate = now.minusMonths(1),
            scheduler = Scheduler(now.minusDays(1), SchedulerRecurrence(PeriodType.MONTH, 1)),
            isArchived = false
        )
        every { budgetRepo.getAll(any(), any()) } returns RepoList(listOf(budget), 1)
        every { budgetRepo.update(any()) } returns Unit
        every { eventRegister.notify(any(), any()) } returns Unit

        val result = useCase.execute(Unit).getOrThrow()
        assertEquals("All due Budget updated", result.message)
        verify(exactly = 1) { budgetRepo.update(any()) }
    }
}
