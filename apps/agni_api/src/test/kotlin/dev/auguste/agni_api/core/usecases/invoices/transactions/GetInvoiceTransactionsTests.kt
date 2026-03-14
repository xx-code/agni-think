package dev.auguste.agni_api.core.usecases.invoices.transactions

import dev.auguste.agni_api.core.adapters.dto.RepoList
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Deduction
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.Transaction
import dev.auguste.agni_api.core.entities.enums.DeductionBaseType
import dev.auguste.agni_api.core.entities.enums.DeductionModeType
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.entities.roundTo
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import dev.auguste.agni_api.core.value_objects.InvoiceDeduction
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals


class GetInvoiceTransactionsTests {

    private val invoiceRepo = mockk<IRepository<Invoice>>(relaxed = true)
    private val deductionRepo = mockk<IRepository<Deduction>>(relaxed = true)
    private val transactionRepo = mockk<IRepository<Transaction>>(relaxed = true)

    private val useCase = GetInvoiceTransactions(
        deductionRepo = deductionRepo,
        transactionRepo = transactionRepo,
        invoiceRepo = invoiceRepo
    )

    private val fixedDate = LocalDateTime.of(2024, 1, 15, 0, 0)
    private val categoryId = UUID.randomUUID()

    private val deduction1 = Deduction(
        title = "deduction 1",
        base = DeductionBaseType.SUBTOTAL,
        mode = DeductionModeType.RATE,
        description = "deduction 1",
    )
    private val deduction2 = Deduction(
        title = "deduction 2",
        base = DeductionBaseType.SUBTOTAL,
        mode = DeductionModeType.RATE,
        description = "deduction 2",
    )

    private val deduction3 = Deduction(
        title = "deduction 3",
        base = DeductionBaseType.SUBTOTAL,
        mode = DeductionModeType.FLAT,
        description = "deduction 3",
    )

    private val invoice1 = Invoice(
        accountId = UUID.randomUUID(),
        status = InvoiceStatusType.COMPLETED,
        mouvementType = InvoiceMouvementType.DEBIT,
        type = InvoiceType.VARIABLECOST,
        date = fixedDate,
        isFreeze = false
    )

    private val invoice2 = Invoice(
        accountId = UUID.randomUUID(),
        status = InvoiceStatusType.COMPLETED,
        mouvementType = InvoiceMouvementType.DEBIT,
        type = InvoiceType.VARIABLECOST,
        date = fixedDate,
        deductions = mutableSetOf(
            InvoiceDeduction(deduction1.id, 1.0),
            InvoiceDeduction(deduction2.id, 1.0)
        ),
        isFreeze = false
    )

    private val invoice3 = Invoice(
        accountId = UUID.randomUUID(),
        status = InvoiceStatusType.COMPLETED,
        mouvementType = InvoiceMouvementType.DEBIT,
        type = InvoiceType.VARIABLECOST,
        date = fixedDate,
        deductions = mutableSetOf(
            InvoiceDeduction(deduction1.id, 1.0),
            InvoiceDeduction(deduction3.id, 2.0)
        ),
        isFreeze = false
    )

    private val transInvoice1 = Transaction(
        invoiceId = invoice1.id,
        categoryId = categoryId,
        amount = 10.0,
        description = "transaction invoice1"
    )

    private val transInvoice2 = Transaction(
        invoiceId = invoice2.id,
        categoryId = categoryId,
        amount = 10.0,
        description = "transaction invoice2"
    )

    private val transInvoice3 = Transaction(
        invoiceId = invoice3.id,
        categoryId = categoryId,
        amount = 10.0,
        description = "transaction invoice3"
    )

    @BeforeEach
    fun setup() {
        every { invoiceRepo.getManyByIds(any()) }
            .returns(listOf(invoice1, invoice2))

        every { deductionRepo.getManyByIds(any()) }
            .returns(listOf(deduction1, deduction2))

        every { transactionRepo.getAll(any(), anyNullable()) }
            .returns(RepoList(listOf(transInvoice1, transInvoice2), 2))
    }

    @Test
    @DisplayName("Retourne les transactions filtrées par categoryId avec déductions appliquées")
    fun shouldReturnFilteredTransactionsWithDeductions() {
        val res = useCase.execAsync(
            GetInvoiceTransactionsInput(
                invoiceIds = setOf(invoice1.id, invoice2.id),
                categoryIds = setOf(categoryId),
                tagIds = null,
                budgetIds = null,
                minAmount = null,
                maxAmount = null,
                doRemoveSpecialCategory = null,
            )
        )

        assertEquals(2, res.size, "Doit retourner une entrée par invoice")

        // invoice1 sans déduction : total = 10.0
        val resultInvoice1 = res.first { it.invoiceId == invoice1.id }
        assertEquals(10.0, resultInvoice1.total, "invoice1 sans déduction")

        val rate1 = invoice2.deductions.first().amount / 100
        val rate2 = invoice2.deductions.toList()[1].amount / 100
        val expectedInvoice2Total = 10.0 * (1 + rate1 + rate2)
        val resultInvoice2 = res.first { it.invoiceId == invoice2.id }
        assertEquals(expectedInvoice2Total, resultInvoice2.total, "invoice2 avec 2 déductions")

        // Total global
        val expectedTotal = 10.0 + expectedInvoice2Total
        assertEquals(expectedTotal, res.sumOf { it.total }, "Total global")
    }

    @Test
    @DisplayName("Return filter transaction with flat deduction")
    fun shouldReturnFilteredTransactionsWithFlatDeductions() {
        every { invoiceRepo.getManyByIds(any()) }
            .returns(listOf(invoice3))

        every { deductionRepo.getManyByIds(any()) }
            .returns(listOf(deduction1, deduction3))

        every { transactionRepo.getAll(any(), anyNullable()) }
            .returns(RepoList(listOf(transInvoice3), 1))

        val res = useCase.execAsync(
            GetInvoiceTransactionsInput(
                invoiceIds = setOf(invoice3.id),
                categoryIds = setOf(categoryId),
                tagIds = null,
                budgetIds = null,
                minAmount = null,
                maxAmount = null,
                doRemoveSpecialCategory = null,
            )
        )

        assertEquals(1, res.size, "Doit retourner une entrée par invoice")

        val rate1 = invoice3.deductions.first().amount / 100
        val rate2 = invoice3.deductions.toList()[1].amount
        val expectedTotal = (10.0) * (1 + rate1 + (rate2/10) )
        // val resultInvoice2 = res.first { it.invoiceId == invoice2.id }
        // assertEquals(expectedInvoice2Total, resultInvoice2.total, "invoice2 avec 2 déductions")

        assertEquals(expectedTotal.roundTo(2), res.sumOf { it.total }, "Total global")
    }
}