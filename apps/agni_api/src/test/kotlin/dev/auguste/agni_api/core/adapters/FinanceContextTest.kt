package dev.auguste.agni_api.core.adapters

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FinanceContextTest {

    private val fundRepo = mockk<IRepository<SavingGoal>>()
    private val categoryRepo = mockk<IRepository<Category>>()
    private val getBalance = mockk<IUseCase<GetBalanceInput, GetBalanceOutput>>()

    private val financeContext = FinanceContext(fundRepo, getBalance, categoryRepo)

    @Test
    fun `returns fund balance when fund exists`() {
        val fundId = UUID.randomUUID()
        val fund = SavingGoal(
            id = fundId,
            title = "Fond pipe",
            description = "",
            target = 300.0,
            balance = 100.0,
            accountId = null
        )
        every { fundRepo.get(fundId) } returns fund

        assertEquals(100.0, financeContext.getFund(fundId).balance)
    }

    @Test
    fun `throws not found when fund does not exist`() {
        val fundId = UUID.randomUUID()
        every { fundRepo.get(fundId) } returns null

        assertFailsWith<DomainException.NotFound.SavingGoal> {
            financeContext.getFund(fundId)
        }
    }

    @Test
    fun `verify fund exists succeeds when fund exists`() {
        val fundId = UUID.randomUUID()
        val fund = SavingGoal(
            id = fundId,
            title = "Fond pipe",
            description = "",
            target = 300.0,
            balance = 100.0,
            accountId = null
        )
        every { fundRepo.get(fundId) } returns fund

        financeContext.verifyFundExists(fundId)
    }

    @Test
    fun `verify fund exists throws not found when fund does not exist`() {
        val fundId = UUID.randomUUID()
        every { fundRepo.get(fundId) } returns null

        assertFailsWith<DomainException.NotFound.SavingGoal> {
            financeContext.verifyFundExists(fundId)
        }
    }

    @Test
    fun `verify category exists succeeds when category exists`() {
        val categoryId = UUID.randomUUID()
        val category = Category(
            id = categoryId,
            title = "Restaurant",
            icon = "",
            color = ""
        )
        every { categoryRepo.get(categoryId) } returns category

        financeContext.verifyCategoryExists(categoryId)
    }

    @Test
    fun `verify category exists throws not found when category does not exist`() {
        val categoryId = UUID.randomUUID()
        every { categoryRepo.get(categoryId) } returns null

        assertFailsWith<DomainException.NotFound.Category> {
            financeContext.verifyCategoryExists(categoryId)
        }
    }

    @Test
    fun `returns category total from balance use case`() {
        val categoryId = UUID.randomUUID()
        val startDate = LocalDate.of(2026, 1, 1)
        val endDate = LocalDate.of(2026, 12, 31)
        every { getBalance.execAsync(any()) } returns GetBalanceOutput(balance = 250.0, income = 300.0, spend = 50.0)

        assertEquals(250.0, financeContext.getCategoryTotal(categoryId, startDate, endDate))

        verify {
            getBalance.execAsync(
                GetBalanceInput(
                    categoryIds = setOf(categoryId),
                    startDate = startDate.atStartOfDay(),
                    endDate = endDate.atStartOfDay()
                )
            )
        }
    }
}
