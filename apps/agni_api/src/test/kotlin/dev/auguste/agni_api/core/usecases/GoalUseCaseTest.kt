package dev.auguste.agni_api.core.usecases

import dev.auguste.agni_api.core.adapters.FinanceContextFund
import dev.auguste.agni_api.core.adapters.IFinanceContext
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.dto.RepoList
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryGoalExtend
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Goal
import dev.auguste.agni_api.core.entities.enums.GoalEvaluationType
import dev.auguste.agni_api.core.entities.enums.GoalStatusType
import dev.auguste.agni_api.core.usecases.goals.CreateGoal
import dev.auguste.agni_api.core.usecases.goals.DeleteGoal
import dev.auguste.agni_api.core.usecases.goals.GetAllGoals
import dev.auguste.agni_api.core.usecases.goals.GetGoal
import dev.auguste.agni_api.core.usecases.goals.UpdateGoal
import dev.auguste.agni_api.core.usecases.goals.dto.CreateGoalInput
import dev.auguste.agni_api.core.usecases.goals.dto.GetAllGoalInput
import dev.auguste.agni_api.core.usecases.goals.dto.UpdateGoalInput
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GoalUseCaseTest {
    private val goalRepo = mockk<IRepository<Goal>>()
    private val financeContext = mockk<IFinanceContext>()

    private val createGoal = CreateGoal(goalRepo = goalRepo, financeContext = financeContext)
    private val updateGoal = UpdateGoal(goalRepo, financeContext)
    private val deleteGoal = DeleteGoal(goalRepo)
    private val getGoal = GetGoal(goalRepo, financeContext)
    private val getAllGoals = GetAllGoals(goalRepo, financeContext)

    private fun goal(
        id: UUID = UUID.randomUUID(),
        title: String = "Fond",
        description: String = "Description",
        sourceId: UUID = UUID.randomUUID(),
        targetAmount: Double = 300.0,
        dueDate: LocalDate = LocalDate.now().plusDays(30),
        status: GoalStatusType = GoalStatusType.ACTIVE,
        type: GoalEvaluationType = GoalEvaluationType.FUND
    ) = Goal(
        id = id,
        title = title,
        description = description,
        targetSourceId = sourceId,
        targetAmount = targetAmount,
        dueDate = dueDate,
        status = status,
        type = type
    )

    private fun createGoalInput(
        sourceId: UUID,
        type: GoalEvaluationType = GoalEvaluationType.FUND,
        targetAmount: Double = 300.0
    ) = CreateGoalInput(
        title = "Fond",
        description = "Description",
        targetAmount = targetAmount,
        targetSourceId = sourceId,
        targetDate = LocalDate.now().plusDays(30),
        status = GoalStatusType.ACTIVE,
        type = type
    )

    @Test
    fun `create fund goal validates source, evaluates progress and persists it`() {
        val sourceId = UUID.randomUUID()
        val goalSlot = slot<Goal>()
        every { financeContext.getFund(sourceId) } returns FinanceContextFund(sourceId, 450.0, 450.0)
        every { goalRepo.create(capture(goalSlot)) } just Runs

        val result = createGoal.execAsync(createGoalInput(sourceId, type = GoalEvaluationType.FUND))

        assertEquals(goalSlot.captured.id, result.newId)
        assertEquals("Fond", goalSlot.captured.title)
        assertEquals(sourceId, goalSlot.captured.targetSourceId)
        assertEquals(GoalEvaluationType.FUND, goalSlot.captured.type)
        verify { financeContext.getFund(sourceId) }
    }

    @Test
    fun `create category target goal validates category and persists it`() {
        val sourceId = UUID.randomUUID()
        val goalSlot = slot<Goal>()
        every { financeContext.verifyCategoryExists(sourceId) } just Runs
        every { financeContext.getCategoryTotal(any(), any(), any()) } returns 50.0
        every { goalRepo.create(capture(goalSlot)) } just Runs

        createGoal.execAsync(createGoalInput(sourceId, type = GoalEvaluationType.TRANSACTION_TARGET))

        assertEquals(sourceId, goalSlot.captured.targetSourceId)
        assertEquals(GoalEvaluationType.TRANSACTION_TARGET, goalSlot.captured.type)
        verify { financeContext.verifyCategoryExists(sourceId) }
    }

    @Test
    fun `does not create goal when fund does not exist`() {
        val sourceId = UUID.randomUUID()
        every { financeContext.getFund(sourceId) } throws DomainException.NotFound.SavingGoal(sourceId)

        assertFailsWith<DomainException.NotFound.SavingGoal> {
            createGoal.execAsync(createGoalInput(sourceId))
        }

        verify(exactly = 0) { goalRepo.create(any()) }
    }

    @Test
    fun `does not create goal when category does not exist`() {
        val sourceId = UUID.randomUUID()
        every { financeContext.verifyCategoryExists(sourceId) } throws DomainException.NotFound.Category(sourceId)

        assertFailsWith<DomainException.NotFound.Category> {
            createGoal.execAsync(createGoalInput(sourceId, type = GoalEvaluationType.TRANSACTION_TARGET))
        }

        verify(exactly = 0) { goalRepo.create(any()) }
    }

    @Test
    fun `create goal throws when no strategy is registered for the type`() {
        val sourceId = UUID.randomUUID()

        assertFailsWith<DomainException.BusinessLogic.GoalStrategyNotExist> {
            createGoal.execAsync(createGoalInput(sourceId, type = GoalEvaluationType.PATRIMONY))
        }

        verify(exactly = 0) { goalRepo.create(any()) }
    }

    @Test
    fun `update goal persists changed fields`() {
        val goalId = UUID.randomUUID()
        val existing = goal(id = goalId, title = "Old title")
        val goalSlot = slot<Goal>()
        every { goalRepo.get(goalId) } returns existing
        every { financeContext.getFund(any()) } returns FinanceContextFund(goalId, 1000.0, 1000.0)
        every { goalRepo.update(capture(goalSlot)) } just Runs

        updateGoal.execAsync(
            UpdateGoalInput(
                id = goalId,
                title = "New title",
                description = null,
                targetAmount = 500.0,
                targetDate = null,
                status = GoalStatusType.PAUSED
            )
        )

        assertEquals("New title", goalSlot.captured.title)
        assertEquals(500.0, goalSlot.captured.targetAmount)
        assertEquals(GoalStatusType.PAUSED, goalSlot.captured.status)
    }

    @Test
    fun `update goal does not persist when nothing changed`() {
        val goalId = UUID.randomUUID()
        val existing = goal(id = goalId, title = "Title", status = GoalStatusType.ACTIVE)
        every { goalRepo.get(goalId) } returns existing

        updateGoal.execAsync(
            UpdateGoalInput(
                id = goalId,
                title = "Title",
                description = "Description",
                targetAmount = 300.0,
                targetDate = existing.dueDate,
                status = GoalStatusType.ACTIVE
            )
        )

        verify(exactly = 0) { goalRepo.update(any()) }
    }

    @Test
    fun `update goal throws not found when goal does not exist`() {
        val goalId = UUID.randomUUID()
        every { goalRepo.get(goalId) } returns null

        assertFailsWith<DomainException.NotFound.Goal> {
            updateGoal.execAsync(UpdateGoalInput(id = goalId, title = null, description = null, targetAmount = null, targetDate = null, status = null))
        }

        verify(exactly = 0) { goalRepo.update(any()) }
    }

    @Test
    fun `get goal returns goal with evaluation`() {
        val goalId = UUID.randomUUID()
        val sourceId = UUID.randomUUID()
        val existing = goal(id = goalId, sourceId = sourceId, targetAmount = 300.0)
        every { goalRepo.get(goalId) } returns existing
        every { financeContext.getFund(sourceId) } returns FinanceContextFund(sourceId, 100.0, 400.0)

        val output = getGoal.execAsync(goalId)

        assertEquals(goalId, output.id)
        assertEquals("Fond", output.title)
        assertEquals(GoalEvaluationType.FUND, output.type)
        assertEquals(100.0, output.evaluation.currentBalance)
        assertEquals((100.0 / 300.0) * 100.0, output.evaluation.progressPercentage)
    }

    @Test
    fun `get goal throws not found when goal does not exist`() {
        val goalId = UUID.randomUUID()
        every { goalRepo.get(goalId) } returns null

        assertFailsWith<DomainException.NotFound.Goal> {
            getGoal.execAsync(goalId)
        }
    }

    @Test
    fun `get all goals returns items with evaluation and forwards filters`() {
        val sourceId = UUID.randomUUID()
        val goal1 = goal(sourceId = sourceId, targetAmount = 300.0)
        val goal2 = goal(sourceId = sourceId, targetAmount = 200.0, status = GoalStatusType.COMPLETED)
        every { financeContext.getFund(any()) } returns FinanceContextFund(sourceId, 50.0, 400.0)
        every { goalRepo.getAll(any(), anyNullable()) } returns RepoList(listOf(goal1, goal2), 2)

        val result = getAllGoals.execAsync(
            GetAllGoalInput(
                queryFilter = QueryFilter(),
                sourceId = sourceId,
                targetDate = null,
                status = GoalStatusType.COMPLETED,
                type = GoalEvaluationType.FUND
            )
        )

        assertEquals(2, result.items.size)
        assertEquals(2L, result.total)
        assertEquals(50.0, result.items.first().evaluation.currentBalance)
        assertEquals(GoalStatusType.COMPLETED, result.items[1].status)

        verify {
            goalRepo.getAll(
                any(),
                match<IQueryExtend<Goal>> {
                    it is QueryGoalExtend && it.sourceId == sourceId && it.status == GoalStatusType.COMPLETED && it.type == GoalEvaluationType.FUND
                }
            )
        }
    }

    @Test
    fun `get all goals returns empty list`() {
        every { financeContext.getFund(any()) } returns FinanceContextFund(UUID.randomUUID(), 0.0, 400.0)
        every { goalRepo.getAll(any(), anyNullable()) } returns RepoList(emptyList(), 0)

        val result = getAllGoals.execAsync(
            GetAllGoalInput(queryFilter = QueryFilter(), sourceId = null, targetDate = null, status = null, type = null)
        )

        assertTrue(result.items.isEmpty())
        assertEquals(0L, result.total)
    }

    @Test
    fun `delete goal deletes when exists`() {
        val goalId = UUID.randomUUID()
        every { goalRepo.get(goalId) } returns goal(id = goalId)
        every { goalRepo.delete(goalId) } just Runs

        deleteGoal.execAsync(goalId)

        verify { goalRepo.delete(goalId) }
    }

    @Test
    fun `delete goal throws not found when goal does not exist`() {
        val goalId = UUID.randomUUID()
        every { goalRepo.get(goalId) } returns null

        assertFailsWith<DomainException.NotFound.Goal> {
            deleteGoal.execAsync(goalId)
        }

        verify(exactly = 0) { goalRepo.delete(goalId) }
    }
}
