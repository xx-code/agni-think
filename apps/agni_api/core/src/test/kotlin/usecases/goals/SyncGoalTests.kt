package usecases.goals

import adapters.IFinanceContext
import adapters.dto.RepoList
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.NotificationEventContent
import adapters.repositories.IRepository
import domain.entities.Goal
import domain.enums.GoalEvaluationType
import domain.enums.GoalStatusType
import domain.value_objects.SchedulerRecurrence
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals

class SyncGoalTests {
    private val eventManager = mockk<IEventRegister>()
    private val goalRepo = mockk<IRepository<Goal>>()
    private val financeContext = mockk<IFinanceContext>()
    private val useCase = SyncGoal(eventManager, goalRepo, financeContext)

    @Test
    fun syncs goal when due() = runBlocking {
        val goal = Goal(
            id = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            title = "Save",
            targetSourceId = UUID.randomUUID(),
            targetAmount = 1000.0,
            dueDate = LocalDate.now().minusDays(1),
            status = GoalStatusType.ACTIVE,
            type = GoalEvaluationType.FUND,
            recurrence = SchedulerRecurrence(domain.enums.PeriodType.MONTH, 1)
        )
        every { goalRepo.getAll(any(), any()) } returns RepoList(listOf(goal), 1)
        every { goalRepo.update(any()) } returns Unit
        every { eventManager.notify(any(), any()) } returns Unit

        val result = useCase.execute(Unit).getOrThrow()
        assertEquals("Sync Goal Success", result.message)
        verify(exactly = 1) { goalRepo.update(any()) }
    }
}
