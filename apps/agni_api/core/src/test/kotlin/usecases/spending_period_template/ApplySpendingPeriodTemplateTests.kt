package usecases.spending_period_template

import adapters.dto.RepoList
import adapters.events.IEventRegister
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Profile
import domain.entities.SpendingPeriod
import domain.entities.SpendingPeriodTemplate
import domain.enums.PeriodType
import domain.value_objects.SchedulerRecurrence
import domain.value_objects.SnapshotForcastSpendingPeriod
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import usecases.interfaces.IUseCase
import usecases.spending_period.dto.ForcastSpendingPeriodInput
import usecases.spending_period.dto.ForcastSpendingPeriodOutput
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals

class ApplySpendingPeriodTemplateTests {
    private val spendingPeriodTemplateRepo = mockk<IRepository<SpendingPeriodTemplate>>()
    private val spendingPeriodRepo = mockk<IRepository<SpendingPeriod>>()
    private val forecastSpendingPeriod = mockk<IUseCase<ForcastSpendingPeriodInput, ForcastSpendingPeriodOutput>>()
    private val profileRepo = mockk<IRepository<Profile>>()
    private val unitOfWork = mockk<IUnitOfWork>()
    private val eventRegister = mockk<IEventRegister>()
    private val useCase = ApplySpendingPeriodTemplate(
        spendingPeriodTemplateRepo,
        spendingPeriodRepo,
        forecastSpendingPeriod,
        profileRepo,
        unitOfWork,
        eventRegister
    )

    @Test
    fun pplies template when start date is past() = runBlocking {
        val now = LocalDate.now()
        val template = SpendingPeriodTemplate(
            id = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            title = "Monthly",
            targetBudgetIds = emptySet(),
            recurrence = SchedulerRecurrence(PeriodType.MONTH, 1),
            startDate = now.minusDays(1),
            isActive = true
        )
        val profile = Profile(id = UUID.randomUUID(), userId = UUID.randomUUID())
        val forecast = ForcastSpendingPeriodOutput(
            expectedRemainAmount = 100.0,
            currentRemainAmount = 0.0,
            totalExpectedIncome = 0.0,
            totalExpectedExpense = 0.0,
            expectedFixExpense = 0.0,
            expectedVariableExpense = 0.0,
            expectedBudgetExpense = 0.0,
            currentIncome = 0.0,
            expectedSaving = 0.0,
            currentSaving = 0.0,
            currentBudgetExpense = 0.0,
            incomeItems = emptyList(),
            fixExpenseItems = emptyList(),
            variableExpenseItems = emptyList(),
            achievedWishedItems = emptyList()
        )

        every { spendingPeriodRepo.getAll(any(), any()) } returns RepoList(emptyList(), 0)
        every { spendingPeriodTemplateRepo.getAll(any(), any()) } returns RepoList(listOf(template), 1)
        every { profileRepo.getAll(any()) } returns RepoList(listOf(profile), 1)
        coEvery { forecastSpendingPeriod.processDirect(any()) } returns forecast
        every { spendingPeriodRepo.create(any()) } returns Unit
        every { spendingPeriodTemplateRepo.update(any()) } returns Unit
        every { eventRegister.notify(any(), any()) } returns Unit

        val result = useCase.execute(Unit).getOrThrow()
        assertEquals("Spending Period Template Complete", result.message)
        verify(exactly = 1) { spendingPeriodRepo.create(any()) }
    }
}
