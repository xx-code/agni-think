package dev.auguste.agni_api.core.usecases.analytics

import dev.auguste.agni_api.core.adapters.dto.RepoList
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.Color
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Profile
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.enums.ContributionAccountType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.entities.enums.ManagementAccountType
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.usecases.analystics.ForcastSpending
import dev.auguste.agni_api.core.usecases.analystics.dto.ForcastSpendingInput
import dev.auguste.agni_api.core.usecases.analystics.dto.ForcastSpendingOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.SavingAdditionalIncomeInput
import dev.auguste.agni_api.core.usecases.analystics.dto.WantItemOutput
import dev.auguste.agni_api.core.usecases.budgets.dto.GetBudgetOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.value_objects.BrokingAccountDetail
import dev.auguste.agni_api.core.value_objects.BusinessAccountDetail
import dev.auguste.agni_api.core.value_objects.CheckingAccountDetail
import dev.auguste.agni_api.core.value_objects.CreditCardAccountDetail
import dev.auguste.agni_api.core.value_objects.SavingAccountDetail
import dev.auguste.agni_api.core.value_objects.Scheduler
import dev.auguste.agni_api.core.value_objects.SchedulerRecurrence
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.util.UUID

class ForcastSpendingTest {
    private val accountRepo = mockk<IRepository<Account>>(relaxed = true)
    private val scheduleInvoiceRepo = mockk<IRepository<ScheduleInvoice>>(relaxed = true)
    private val budgetRepo = mockk<IRepository<Budget>>(relaxed = true)
    private val profileRepo = mockk<IRepository<Profile>>(relaxed = true)
    private val getBudget = mockk<IUseCase<UUID, GetBudgetOutput>>(relaxed = true)
    private val getBalance = mockk<IUseCase<GetBalanceInput, GetBalanceOutput>>(relaxed = true)

    private val forcastSpending = ForcastSpending(
        scheduleInvoiceRepo = scheduleInvoiceRepo,
        accountRepo = accountRepo,
        budgetRepo = budgetRepo,
        profileRepo = profileRepo,
        getBudget = getBudget,
        getBalance = getBalance
    )

    private val endDate = LocalDate.of(2026, Month.AUGUST, 27)

    @BeforeEach
    fun stubDefaultGetBalance() {
        every { getBalance.execAsync(any()) } returns GetBalanceOutput(balance = 0.0, income = 0.0, spend = 0.0)
    }

    private fun stubProfile(savingRate: Double) {
        every { profileRepo.getAll(any()) } returns RepoList(
            items = listOf(Profile(savingPercentage = savingRate)),
            total = 1L
        )
    }

    private fun account(balance: Double, type: AccountType, id: UUID = UUID.randomUUID()): Account {
        return Account(
            id = id,
            title = "Account",
            balance = balance,
            color = Color("#fff"),
            detail = when (type) {
                AccountType.CHECKING -> CheckingAccountDetail(0.0)
                AccountType.SAVING -> SavingAccountDetail(0.0)
                AccountType.CREDIT_CARD -> CreditCardAccountDetail(0.0, LocalDate.now())
                AccountType.BROKING -> BrokingAccountDetail(ManagementAccountType.MANAGED, ContributionAccountType.UNREGISTERED)
                AccountType.BUSINESS -> BusinessAccountDetail(0.0)
            },
            currencyId = null
        )
    }

    private fun stubAccountList(accounts: List<Account>) {
        every { accountRepo.getAll(any()) } returns RepoList(accounts, accounts.size.toLong())
    }

    private fun stubAccounts(vararg details: Pair<Double, AccountType>) {
        stubAccountList(details.map { (balance, type) -> account(balance, type) })
    }

    private fun stubScheduleInvoices(vararg invoices: ScheduleInvoice) {
        every { scheduleInvoiceRepo.getAll(any(), any()) } returns RepoList(
            items = invoices.toList(),
            total = invoices.size.toLong()
        )
    }

    private fun stubNoScheduleInvoices() {
        every { scheduleInvoiceRepo.getAll(any(), any()) } returns RepoList(emptyList(), 0L)
    }

    private fun stubNoBudgets() {
        every { budgetRepo.getManyByIds(any()) } returns emptyList()
    }

    private fun scheduleInvoice(
        type: InvoiceType,
        amount: Double,
        isFreeze: Boolean = false,
        freezeEndDate: LocalDate? = null
    ): ScheduleInvoice {
        return ScheduleInvoice(
            title = "invoice",
            accountId = UUID.randomUUID(),
            type = type,
            amount = amount,
            scheduler = Scheduler(LocalDateTime.of(2026, Month.AUGUST, 15, 0, 0)),
            categoryId = UUID.randomUUID(),
            freezeScheduler = if (isFreeze) Scheduler(freezeEndDate!!.atStartOfDay()) else null,
            isFreeze = isFreeze
        )
    }

    private fun emptyInput(): ForcastSpendingInput = ForcastSpendingInput(
        startDate = LocalDate.of(2026, Month.AUGUST, 13),
        endDate = endDate,
        wantItems = listOf(),
        savingAdditionalIncome = listOf(),
        budgetIds = listOf()
    )

    @Test
    fun `empty forecast uses override balance and returns zeroes`() {
        stubNoBudgets()
        stubNoScheduleInvoices()
        stubProfile(10.0)

        val result = forcastSpending.execAsync(
            emptyInput().copy(overrideAccountsBalance = 100.0, savingRate = 10.0)
        )

        assertEquals(
            ForcastSpendingOutput(
                remainAmount = 100.0,
                totalExpectedIncome = 100.0,
                totalExpectedExpense = 0.0,
                expectedIncome = 0.0,
                expectedFixExpense = 0.0,
                expectedVariableExpense = 0.0,
                expectedPlanFreezeExpense = 0.0,
                expectedBudgetExpense = 0.0,
                expectedSaving = 0.0,
                itemsApproved = listOf(),
                itemsRejected = listOf()
            ),
            result
        )
    }

    @Test
    fun `current balance excludes saving and broking accounts`() {
        stubNoBudgets()
        stubNoScheduleInvoices()
        stubProfile(0.0)
        stubAccounts(
            50.0 to AccountType.CHECKING,
            100.0 to AccountType.SAVING,
            200.0 to AccountType.BROKING,
            -10.0 to AccountType.CREDIT_CARD,
            20.0 to AccountType.BUSINESS
        )

        val result = forcastSpending.execAsync(emptyInput().copy(savingRate = 0.0))

        assertEquals(60.0, result.remainAmount)
        assertEquals(60.0, result.totalExpectedIncome)
        assertEquals(0.0, result.totalExpectedExpense)
    }

    @Test
    fun `sums income fixed and variable expenses from schedule invoices`() {
        stubNoBudgets()
        stubProfile(10.0)
        stubScheduleInvoices(
            scheduleInvoice(InvoiceType.INCOME, 1000.0),
            scheduleInvoice(InvoiceType.FIXEDCOST, 200.0),
            scheduleInvoice(InvoiceType.VARIABLECOST, 50.0),
            scheduleInvoice(InvoiceType.OTHER, 999.0)
        )

        val result = forcastSpending.execAsync(
            emptyInput().copy(overrideAccountsBalance = 500.0, savingRate = 10.0)
        )

        assertEquals(1000.0, result.expectedIncome)
        assertEquals(200.0, result.expectedFixExpense)
        assertEquals(50.0, result.expectedVariableExpense)
        assertEquals(0.0, result.expectedPlanFreezeExpense)
        assertEquals(0.0, result.expectedBudgetExpense)
        assertEquals(100.0, result.expectedSaving)
        assertEquals(1500.0, result.totalExpectedIncome)
        assertEquals(350.0, result.totalExpectedExpense)
        assertEquals(1150.0, result.remainAmount)
    }

    @Test
    fun `multiplies recurring schedule invoices by number of occurrences`() {
        stubNoBudgets()
        stubProfile(0.0)
        stubScheduleInvoices(
            ScheduleInvoice(
                title = "weekly income",
                accountId = UUID.randomUUID(),
                type = InvoiceType.INCOME,
                amount = 100.0,
                scheduler = Scheduler(
                    date = LocalDateTime.of(2026, Month.AUGUST, 13, 0, 0),
                    repeater = SchedulerRecurrence(PeriodType.WEEK, 1)
                ),
                categoryId = UUID.randomUUID(),
                freezeScheduler = null,
                isFreeze = false
            )
        )

        val result = forcastSpending.execAsync(
            emptyInput().copy(overrideAccountsBalance = 0.0, savingRate = 0.0)
        )

        assertEquals(200.0, result.expectedIncome)
        assertEquals(200.0, result.totalExpectedIncome)
        assertEquals(0.0, result.totalExpectedExpense)
        assertEquals(200.0, result.remainAmount)
    }

    @Test
    fun `counts only freeze invoices whose freeze end date is after the forecast end date`() {
        stubNoBudgets()
        stubProfile(0.0)
        every { getBalance.execAsync(any()) } returns GetBalanceOutput(balance = 100.0, income = 0.0, spend = 0.0)
        stubScheduleInvoices(
            scheduleInvoice(InvoiceType.FIXEDCOST, 200.0),
            scheduleInvoice(
                InvoiceType.VARIABLECOST,
                300.0,
                isFreeze = true,
                freezeEndDate = LocalDate.of(2026, Month.SEPTEMBER, 10)
            ),
            scheduleInvoice(
                InvoiceType.VARIABLECOST,
                100.0,
                isFreeze = true,
                freezeEndDate = LocalDate.of(2026, Month.AUGUST, 1)
            )
        )

        val result = forcastSpending.execAsync(
            emptyInput().copy(overrideAccountsBalance = 1000.0, savingRate = 0.0)
        )

        assertEquals(400.0, result.expectedVariableExpense)
        assertEquals(200.0, result.expectedPlanFreezeExpense)
        assertEquals(200.0, result.expectedFixExpense)
        assertEquals(800.0, result.totalExpectedExpense)
        assertEquals(200.0, result.remainAmount)
    }

    @Test
    fun `adds budget expenses for requested budget ids`() {
        stubNoScheduleInvoices()
        stubProfile(0.0)

        val aId = UUID.randomUUID()
        val bId = UUID.randomUUID()
        val a = Budget(
            id = aId,
            title = "a",
            target = 1000.0,
            scheduler = Scheduler(LocalDateTime.of(2026, Month.AUGUST, 20, 0, 0))
        )
        val b = Budget(
            id = bId,
            title = "b",
            target = 500.0,
            scheduler = Scheduler(LocalDateTime.of(2026, Month.AUGUST, 20, 0, 0))
        )

        every { budgetRepo.getManyByIds(any()) } returns listOf(a, b)
        every { getBudget.execAsync(aId) } returns GetBudgetOutput(
            id = aId, title = "a", target = 1000.0, currentBalance = 300.0,
            dueDate = LocalDateTime.now(), repeater = null
        )
        every { getBudget.execAsync(bId) } returns GetBudgetOutput(
            id = bId, title = "b", target = 500.0, currentBalance = 100.0,
            dueDate = LocalDateTime.now(), repeater = null
        )

        val result = forcastSpending.execAsync(
            emptyInput().copy(
                overrideAccountsBalance = 0.0,
                savingRate = 0.0,
                budgetIds = listOf(aId, bId)
            )
        )

        assertEquals(1100.0, result.expectedBudgetExpense)
        verify(exactly = 1) { getBudget.execAsync(aId) }
        verify(exactly = 1) { getBudget.execAsync(bId) }
    }

    @Test
    fun `uses profile saving rate when input saving rate is null`() {
        stubNoBudgets()
        stubProfile(20.0)
        stubScheduleInvoices(scheduleInvoice(InvoiceType.INCOME, 1000.0))

        val result = forcastSpending.execAsync(
            emptyInput().copy(overrideAccountsBalance = 0.0, savingRate = null)
        )

        assertEquals(200.0, result.expectedSaving)
        assertEquals(200.0, result.totalExpectedExpense)
        assertEquals(800.0, result.remainAmount)
    }

    @Test
    fun `input saving rate overrides profile saving rate`() {
        stubNoBudgets()
        stubProfile(80.0)
        stubScheduleInvoices(scheduleInvoice(InvoiceType.INCOME, 1000.0))

        val result = forcastSpending.execAsync(
            emptyInput().copy(overrideAccountsBalance = 0.0, savingRate = 25.0)
        )

        assertEquals(250.0, result.expectedSaving)
    }

    @Test
    fun `approves want items that fit within available amount`() {
        stubNoBudgets()
        stubNoScheduleInvoices()
        stubProfile(0.0)

        val result = forcastSpending.execAsync(
            emptyInput().copy(
                overrideAccountsBalance = 1000.0,
                savingRate = 0.0,
                wantItems = listOf(
                    WantItemOutput("a", 200.0),
                    WantItemOutput("b", 300.0),
                    WantItemOutput("c", 800.0)
                )
            )
        )

        assertEquals(1000.0, result.remainAmount)
        assertEquals(listOf(WantItemOutput("a", 200.0), WantItemOutput("b", 300.0)), result.itemsApproved)
        assertEquals(listOf<WantItemOutput>(), result.itemsRejected)
    }

    @Test
    fun `rejects want items that exceed available balance after saving margin`() {
        stubNoBudgets()
        stubNoScheduleInvoices()
        stubProfile(0.0)

        val result = forcastSpending.execAsync(
            emptyInput().copy(
                overrideAccountsBalance = 1000.0,
                savingRate = 20.0,
                wantItems = listOf(
                    WantItemOutput("big", 900.0),
                    WantItemOutput("small", 100.0)
                )
            )
        )

        assertEquals(1000.0, result.remainAmount)
        assertEquals(listOf(WantItemOutput("small", 100.0)), result.itemsApproved)
        assertEquals(listOf<WantItemOutput>(), result.itemsRejected)
    }

    @Test
    fun `adds additional saving income on top of override balance`() {
        stubNoBudgets()
        stubNoScheduleInvoices()
        stubProfile(0.0)
        val savingId = UUID.randomUUID()
        stubAccountList(listOf(account(balance = 1000.0, type = AccountType.SAVING, id = savingId)))

        val result = forcastSpending.execAsync(
            emptyInput().copy(
                overrideAccountsBalance = 500.0,
                savingRate = 0.0,
                savingAdditionalIncome = listOf(SavingAdditionalIncomeInput(savingId, 200.0))
            )
        )

        assertEquals(0.0, result.expectedIncome)
        assertEquals(700.0, result.totalExpectedIncome)
        assertEquals(0.0, result.totalExpectedExpense)
        assertEquals(700.0, result.remainAmount)
    }

    @Test
    fun `adds additional saving income on top of scheduled income`() {
        stubNoBudgets()
        stubProfile(10.0)
        val savingId = UUID.randomUUID()
        stubAccountList(listOf(account(balance = 300.0, type = AccountType.SAVING, id = savingId)))
        stubScheduleInvoices(scheduleInvoice(InvoiceType.INCOME, 500.0))

        val result = forcastSpending.execAsync(
            emptyInput().copy(
                overrideAccountsBalance = 0.0,
                savingRate = 10.0,
                savingAdditionalIncome = listOf(SavingAdditionalIncomeInput(savingId, 100.0))
            )
        )

        assertEquals(500.0, result.expectedIncome)
        assertEquals(600.0, result.totalExpectedIncome)
        assertEquals(50.0, result.expectedSaving)
        assertEquals(50.0, result.totalExpectedExpense)
        assertEquals(550.0, result.remainAmount)
    }

    @Test
    fun `throws when additional saving account is not a saving account`() {
        stubNoBudgets()
        stubNoScheduleInvoices()
        stubProfile(0.0)
        val checkingId = UUID.randomUUID()
        stubAccountList(listOf(account(balance = 1000.0, type = AccountType.CHECKING, id = checkingId)))

        assertThrows(DomainException.NotFound.ManyAccounts::class.java) {
            forcastSpending.execAsync(
                emptyInput().copy(
                    overrideAccountsBalance = 0.0,
                    savingRate = 0.0,
                    savingAdditionalIncome = listOf(SavingAdditionalIncomeInput(checkingId, 200.0))
                )
            )
        }
    }

    @Test
    fun `throws when additional saving account does not exist`() {
        stubNoBudgets()
        stubNoScheduleInvoices()
        stubProfile(0.0)
        stubAccountList(listOf())

        assertThrows(DomainException.NotFound.ManyAccounts::class.java) {
            forcastSpending.execAsync(
                emptyInput().copy(
                    overrideAccountsBalance = 0.0,
                    savingRate = 0.0,
                    savingAdditionalIncome = listOf(SavingAdditionalIncomeInput(UUID.randomUUID(), 200.0))
                )
            )
        }
    }

    @Test
    fun `throws when additional saving amount exceeds the saving account balance`() {
        stubNoBudgets()
        stubNoScheduleInvoices()
        stubProfile(0.0)
        val savingId = UUID.randomUUID()
        stubAccountList(listOf(account(balance = 100.0, type = AccountType.SAVING, id = savingId)))

        assertThrows(DomainException.BusinessLogic.ForcastAdditionalSavingAmountMustLessThanBalance::class.java) {
            forcastSpending.execAsync(
                emptyInput().copy(
                    overrideAccountsBalance = 0.0,
                    savingRate = 0.0,
                    savingAdditionalIncome = listOf(SavingAdditionalIncomeInput(savingId, 200.0))
                )
            )
        }
    }
}
