package usecases.invoices

import adapters.dto.RepoList
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.NotificationEventContent
import adapters.repositories.IQueryExtendBuilder
import adapters.repositories.IRepository
import domain.entities.Account
import domain.entities.Invoice
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import usecases.invoices.dto.DeleteInvoiceInput
import usecases.interfaces.IUseCase
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals

class RemoveFreezeInvoiceTests {
    private val invoiceRepo = mockk<IRepository<Invoice>>()
    private val accountRepo = mockk<IRepository<Account>>()
    private val deleteInvoice = mockk<IUseCase<DeleteInvoiceInput, Unit>>()
    private val eventRegister = mockk<IEventRegister>()
    private val useCase = RemoveFreezeInvoice(invoiceRepo, accountRepo, deleteInvoice, eventRegister)

    @Test
    fun emoves freeze invoices past date() = runBlocking {
        val account = Account(
            id = UUID.randomUUID(),
            title = "Main",
            balance = 100.0,
            userId = UUID.randomUUID()
        )
        val freezeInvoice = Invoice(
            id = UUID.randomUUID(),
            title = "Freeze",
            accountId = account.id,
            userId = UUID.randomUUID(),
            date = LocalDate.now().minusDays(1)
        )
        every { invoiceRepo.getAll(any(), any()) } returns RepoList(listOf(freezeInvoice), 1)
        every { accountRepo.get(account.id) } returns account
        coEvery { deleteInvoice.processDirect(any()) } returns Unit
        every { eventRegister.notify(any(), any()) } returns Unit

        val result = useCase.execute(Unit).getOrThrow()
        assertEquals("Remove Freeze transaction successfully", result.message)
        coVerify(exactly = 1) { deleteInvoice.processDirect(DeleteInvoiceInput(freezeInvoice.id, false)) }
    }
}
