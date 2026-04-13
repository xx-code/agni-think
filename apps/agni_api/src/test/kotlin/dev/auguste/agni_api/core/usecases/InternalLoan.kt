package dev.auguste.agni_api.core.usecases.invoices.transactions

import dev.auguste.agni_api.core.adapters.dto.RepoList
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.InternalLoan
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.internal_loan.CreateInternalLoan
import dev.auguste.agni_api.core.usecases.internal_loan.dto.CreateInternalLoanInput
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import dev.auguste.agni_api.core.value_objects.CreditCardAccountDetail
import dev.auguste.agni_api.core.value_objects.SavingAccountDetail
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class CreateInternalLoanTest {

    private val internalLoanRepo = mockk<IRepository<InternalLoan>>()
    private val accountRepo = mockk<IRepository<Account>>()
    private val createInvoice = mockk<IInnerUseCase<CreateInvoiceInput, CreatedOutput>>()
    private val invoiceRepo = mockk<IRepository<Invoice>>()
    private val scheduleInvoiceRepo = mockk<IRepository<ScheduleInvoice>>()
    private val getInvoice = mockk<IUseCase<UUID, GetInvoiceOutput>>()
    private val unitOfWork = mockk<IUnitOfWork>()

    private val useCase = CreateInternalLoan(
        internalLoanRepo, accountRepo, createInvoice,
        invoiceRepo, scheduleInvoiceRepo, getInvoice, unitOfWork
    )

    @Test
    fun `should accept loan when confidence score is above 80 percent`() {
        // Arrange
        val fundId = UUID.randomUUID()
        val creditId = UUID.randomUUID()
        val invoiceId = UUID.randomUUID()
        val input = CreateInternalLoanInput(
            fundSourceId = fundId,
            creditTargetId = creditId,
            dueDate = LocalDate.of(2026, 4, 30),
            invoiceInput = mockk()
        )

        // Mock Unit of Work execution
        every { unitOfWork.execute<CreatedOutput>(any()) } answers {
            val block = firstArg<() -> CreatedOutput>()
            block()
        }

        // Mock Accounts (Your Profile)
        val savingsAccount = mockk<Account> {
            every { balance } returns 8997.0
            every { detail } returns SavingAccountDetail(2000.0)
        }
        val creditAccount = mockk<Account> {
            every { balance } returns 300.0 // 6% of a 5k limit
            every { detail } returns CreditCardAccountDetail(13000.0, LocalDate.of(2026, 4, 12))
        }
        every { accountRepo.get(fundId) } returns savingsAccount
        every { accountRepo.get(creditId) } returns creditAccount

        // Mock Repos (Empty lists for simplicity in this path)
        every { internalLoanRepo.getAll(any(), any()) } returns RepoList(emptyList(), 0)
        every { invoiceRepo.getAll(any(), any()) } returns RepoList(emptyList(), 0)

        // Mock Scheduled Income (2 x 1927.0)
        val biWeeklyIncome = mockk<ScheduleInvoice> {
            every { amount } returns 1927.0
            every { scheduler.repeater?.computeOccurrences(any(), any()) } returns 2
        }
        every { scheduleInvoiceRepo.getAll(any(), any()) } returns RepoList(listOf(biWeeklyIncome), 1)

        // Mock Invoice Creation
        every { createInvoice.execInnerAsync(any()) } returns CreatedOutput(invoiceId)
        every { getInvoice.execAsync(invoiceId) } returns GetInvoiceOutput(
            id = invoiceId,
            total = 464.0,
            date = LocalDate.now().atStartOfDay(),
            accountId = UUID.randomUUID(),
            status = InvoiceStatusType.PENDING.value,
            subTotal = 464.0,
            type = InvoiceType.VARIABLECOST.value,
            mouvement = InvoiceMouvementType.DEBIT.value,
            transactions = listOf(),
            deductions = listOf()
        )

        every { internalLoanRepo.create(any()) } just runs

        // Act
        val result = useCase.execAsync(input)

        // Assert
        assertNotNull(result)
        verify(exactly = 1) { internalLoanRepo.create(any()) }
    }

    @Test
    fun `should throw exception when confidence score is too low`() {
        // Arrange - Simulate very low savings
        val fundId = UUID.randomUUID()
        val creditId = UUID.randomUUID()

        every { unitOfWork.execute<CreatedOutput>(any()) } answers { firstArg<() -> CreatedOutput>().invoke() }
        every { accountRepo.get(fundId) } returns mockk {
            every { balance } returns 100.0 // Extremely low savings
            every { detail } returns SavingAccountDetail(2000.0)
        }
        every { accountRepo.get(creditId) } returns mockk {
            every { balance } returns 4500.0 // 6% of a 5k limit
            every { detail } returns CreditCardAccountDetail(5000.0, LocalDate.of(2026, 4, 12))
        }

        // Mock a large purchase (1000$) on tiny savings
        every { internalLoanRepo.getAll(any(), any()) } returns RepoList(emptyList(), 0)
        every { invoiceRepo.getAll(any(), any()) } returns RepoList(emptyList(), 0)
        every { scheduleInvoiceRepo.getAll(any(), any()) } returns RepoList(emptyList(), 0)
        every { createInvoice.execInnerAsync(any()) } returns CreatedOutput(UUID.randomUUID())
        every { getInvoice.execAsync(any()) } returns GetInvoiceOutput(
            id = UUID.randomUUID(),
            total = 1000.0,
            date = LocalDate.now().atStartOfDay(),
            accountId = UUID.randomUUID(),
            status = InvoiceStatusType.PENDING.value,
            subTotal = 1000.0,
            type = InvoiceType.VARIABLECOST.value,
            mouvement = InvoiceMouvementType.DEBIT.value,
            transactions = listOf(),
            deductions = listOf()
            )

        // Act & Assert
        assertThrows(Exception::class.java) {
            useCase.execAsync(mockk {
                every { fundSourceId } returns fundId
                every { creditTargetId } returns creditId
                every { dueDate } returns LocalDate.now().plusDays(5)
            })
        }
    }
}