package usecases

import adapters.FinanceContextFund
import adapters.IFinanceContext
import adapters.dto.QueryFilter
import adapters.dto.RepoList
import adapters.repositories.IQueryExtendBuilder
import adapters.repositories.IRepository
import domain.entities.Goal
import domain.enums.GoalEvaluationType
import domain.enums.GoalStatusType
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
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
import usecases.goals.CreateGoal
import usecases.goals.DeleteGoal
import usecases.goals.GetAllGoals
import usecases.goals.GetGoal
import usecases.goals.UpdateGoal
import usecases.goals.dto.CreateGoalInput
import usecases.goals.dto.GetAllGoalInput
import usecases.goals.dto.UpdateGoalInput

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
        every { financeContext.getFund(sourceId) } throws NotFoundException.SingleEntity(sourceId, "saving_goal")

        assertFailsWith<NotFoundException> {
            createGoal.execAsync(createGoalInput(sourceId))
        }

        verify(exactly = 0) { goalRepo.create(any()) }
    }

    @Test
    fun `does not create goal when category does not exist`() {
        val sourceId = UUID.randomUUID()
        every { financeContext.verifyCategoryExists(sourceId) } throws NotFoundException.SingleEntity(sourceId, "category")

        assertFailsWith<NotFoundException> {
            createGoal.execAsync(createGoalInput(sourceId, type = GoalEvaluationType.TRANSACTION_TARGET))
        }

        verify(exactly = 0) { goalRepo.create(any()) }
    }

    @Test
    fun `create goal throws when no strategy is registered for the type`() {
        val sourceId = UUID.randomUUID()

        assertFailsWith<ValidationException> {
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

        assertFailsWith<NotFoundException> {
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

        assertFailsWith<NotFoundException> {
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
                match<IQueryExtendBuilder<Goal>> { builder ->
                    // `GetAllGoals` construit un `QueryExtendBuilder<Goal>` generique :
                    // le filtrage se fait par conditions, plus par une classe dediee.
                    val conditions = builder.getConditions()
                    fun valueOf(field: String) = conditions.firstOrNull { it.fieldName == field }?.value

                    valueOf("targetSourceId") == setOf(sourceId) &&
                        valueOf("status") == GoalStatusType.COMPLETED &&
                        valueOf("type") == setOf(GoalEvaluationType.FUND.value)
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

        assertFailsWith<NotFoundException> {
            deleteGoal.execAsync(goalId)
        }

        verify(exactly = 0) { goalRepo.delete(goalId) }
    }
}
