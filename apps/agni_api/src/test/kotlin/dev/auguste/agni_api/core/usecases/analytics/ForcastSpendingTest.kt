package dev.auguste.agni_api.core.usecases.analytics

import dev.auguste.agni_api.core.adapters.dto.RepoList
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.Color
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.usecases.analystics.ForcastSpending
import dev.auguste.agni_api.core.usecases.analystics.dto.ForcastSpendingInput
import dev.auguste.agni_api.core.usecases.analystics.dto.ForcastSpendingOutput
import dev.auguste.agni_api.core.value_objects.CheckingAccountDetail
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.Month
import kotlin.test.Test
import kotlin.test.assertEquals

class ForcastSpendingTest {
    private val accountRepo = mockk<IRepository<Account>>()
    private val scheduleInvoiceRepo = mockk<IRepository<ScheduleInvoice>>() 
    private val budgetRepo = mockk<IRepository<Budget>>()
    
    private val forcastSpending = ForcastSpending(
        scheduleInvoiceRepo = scheduleInvoiceRepo,
        accountRepo = accountRepo,
        budgetRepo = budgetRepo
    )

    @Test
    fun `Simple forcast of two week`() {
        val expected = ForcastSpendingOutput(
            remainAmount = 375.0,
            totalExpectedIncome = 1450.0,
            totalExpectedExpense = 795.0,
            expectedIncome = 1400.0,
            expectedFixExpense = 450.0,
            expectedVariableExpense = 95.0,
            expectedBudgetExpense = 250.0,
            expectedSaving = 280.0,
            itemsApproved = listOf(),
            itemsRejected = listOf()
        )

        val startDate = LocalDate.of(2026, Month.AUGUST, 13)
        val endDate = startDate.plusWeeks(2)

        every { accountRepo.getAll(any()) } returns RepoList(
            items = listOf(
                Account(
                    title = "Account",
                    balance = 50.0,
                    color = Color("#fff"),
                    detail = CheckingAccountDetail(0.0),
                    currencyId = null
                )
            ),
            total = 1L
        )

        val forcast = forcastSpending.execAsync(ForcastSpendingInput(
            startDate = startDate,
            endDate = endDate,
            wantItems = listOf(),
            savingAdditionalIncome = listOf(),
            savingRate = 20.0
        ))


        assertEquals(expected, forcast)
    }
}