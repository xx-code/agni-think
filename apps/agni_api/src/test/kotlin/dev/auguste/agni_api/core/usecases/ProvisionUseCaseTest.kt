package dev.auguste.agni_api.core.usecases

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.dto.RepoList
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.entities.enums.DepreciationType
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.entities.enums.ProvisionType
import dev.auguste.agni_api.core.usecases.provisionable.CreateProvisionable
import dev.auguste.agni_api.core.usecases.provisionable.DeleteProvisionable
import dev.auguste.agni_api.core.usecases.provisionable.GetAllProvisionable
import dev.auguste.agni_api.core.usecases.provisionable.GetProvision
import dev.auguste.agni_api.core.usecases.provisionable.ProvisionCommon
import dev.auguste.agni_api.core.usecases.provisionable.UpdateProvisionable
import dev.auguste.agni_api.core.usecases.provisionable.dto.CreateProvisionInput
import dev.auguste.agni_api.core.usecases.provisionable.dto.DeleteProvisionInput
import dev.auguste.agni_api.core.usecases.provisionable.dto.ScheduleInvoiceProvisionInput
import dev.auguste.agni_api.core.usecases.provisionable.dto.UpdateProvisionInput
import dev.auguste.agni_api.core.value_objects.ProvisionDepreciateCriteria
import dev.auguste.agni_api.core.value_objects.ProvisionPayment
import dev.auguste.agni_api.core.value_objects.Scheduler
import dev.auguste.agni_api.core.value_objects.SchedulerRecurrence
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

private val ACQUISITION_DATE: LocalDate = LocalDate.of(2024, 1, 1)

private fun buildPlainProvision(
    id: UUID = UUID.randomUUID(),
    title: String = "MacBook",
    initialCost: Double = 1200.0,
    criteria: MutableList<ProvisionDepreciateCriteria> = mutableListOf(
        ProvisionDepreciateCriteria("Half", "Half value", DepreciationType.FIX, 600.0)
    ),
    expectedLifespanMonth: Int = 12,
    floorValue: Double = 0.0
): Provision {
    return Provision(
        id = id,
        title = title,
        initialCost = initialCost,
        isPatrimony = false,
        acquisitionDate = ACQUISITION_DATE,
        expectedLifespanMonth = expectedLifespanMonth,
        depreciationCriteria = criteria,
        floorValue = floorValue
    )
}

private fun buildLoanProvision(
    id: UUID = UUID.randomUUID(),
    title: String = "Voiture",
    initialCost: Double = 12000.0,
    interestLoan: Double = 12.0,
    loanMonth: Long = 12,
    expectedLifespanMonth: Int = 24,
    paymentInfo: ProvisionPayment? = null
): Provision {
    return Provision(
        id = id,
        title = title,
        initialCost = initialCost,
        isPatrimony = true,
        acquisitionDate = ACQUISITION_DATE,
        expectedLifespanMonth = expectedLifespanMonth,
        depreciationCriteria = mutableListOf(),
        floorValue = 0.0,
        type = ProvisionType.DEPRECIATE_LOAN,
        paymentInfo = paymentInfo,
        interestLoan = interestLoan,
        loanMonth = loanMonth
    )
}

private fun buildScheduleInvoiceInput(
    scheduler: Scheduler = Scheduler(LocalDateTime.of(2025, 3, 10, 0, 0), SchedulerRecurrence(PeriodType.MONTH, 1))
): ScheduleInvoiceProvisionInput {
    return ScheduleInvoiceProvisionInput(
        invoiceAccountId = UUID.randomUUID(),
        invoiceCategoryId = UUID.randomUUID(),
        scheduler = scheduler,
        endDate = LocalDate.of(2026, 1, 1),
        tagIds = emptySet(),
        budgetIds = emptySet()
    )
}

private fun buildUnitOfWork(): IUnitOfWork {
    return object : IUnitOfWork {
        override fun <T> execute(block: () -> T): T = block()
    }
}

private fun mockRepo(): IRepository<Provision> = mockk(relaxed = true)

class CreateProvisionableTests {

    @Test
    fun `create provision and return its id`() {
        val repo = mockRepo()
        every { repo.existsByName(any()) } returns false
        val useCase = CreateProvisionable(repo)
        val input = CreateProvisionInput(
            title = "MacBook",
            initialCost = 1200.0,
            acquisitionDate = ACQUISITION_DATE,
            expectedLifespanMonth = 12,
            type = ProvisionType.DEPRECIATE,
            isPatrimony = false,
            depreciationCriteria = listOf(
                ProvisionDepreciateCriteria("Half", "Half value", DepreciationType.FIX, 600.0)
            ),
            floorValue = 100.0
        )

        val result = useCase.execAsync(input)

        val createdSlot = slot<Provision>()
        verify(exactly = 1) { repo.create(capture(createdSlot)) }

        val created = createdSlot.captured
        assertEquals(result.newId, created.id)
        assertEquals("MacBook", created.title)
        assertEquals(1200.0, created.initialCost)
        assertEquals(ACQUISITION_DATE, created.acquisitionDate)
        assertEquals(12, created.expectedLifespanMonth)
        assertEquals(false, created.isPatrimony)
        assertEquals(100.0, created.floorValue)
        assertEquals(ProvisionType.DEPRECIATE, created.type)
        assertEquals(1, created.depreciationCriteria.size)
        assertNull(created.paymentInfo)
    }

    @Test
    fun `throw when a provision already exists with the same name`() {
        val repo = mockRepo()
        every { repo.existsByName("MacBook") } returns true
        val useCase = CreateProvisionable(repo)
        val input = CreateProvisionInput(
            title = "MacBook",
            initialCost = 1200.0,
            acquisitionDate = ACQUISITION_DATE,
            expectedLifespanMonth = 12,
            type = ProvisionType.DEPRECIATE,
            isPatrimony = false,
            depreciationCriteria = emptyList()
        )

        assertThrows(DomainException.AlreadyExist.Provisionable::class.java) {
            useCase.execAsync(input)
        }

        verify(exactly = 0) { repo.create(any()) }
    }

    @Test
    fun `attach computed payment info when creating loan provision with schedule invoice`() {
        val repo = mockRepo()
        every { repo.existsByName(any()) } returns false
        val useCase = CreateProvisionable(repo)
        val scheduleInvoice = buildScheduleInvoiceInput()
        val input = CreateProvisionInput(
            title = "Voiture",
            initialCost = 12000.0,
            acquisitionDate = ACQUISITION_DATE,
            expectedLifespanMonth = 24,
            type = ProvisionType.DEPRECIATE_LOAN,
            isPatrimony = true,
            depreciationCriteria = emptyList(),
            scheduleInvoice = scheduleInvoice,
            interestLoan = 12.0,
            loanMonth = 12
        )

        useCase.execAsync(input)

        val createdSlot = slot<Provision>()
        verify(exactly = 1) { repo.create(capture(createdSlot)) }

        val payment = createdSlot.captured.paymentInfo
        assertNotNull(payment)
        assertEquals(scheduleInvoice.invoiceAccountId, payment!!.accountId)
        assertEquals(scheduleInvoice.invoiceCategoryId, payment.categoryId)
        assertEquals(scheduleInvoice.scheduler, payment.scheduler)
        assertEquals(scheduleInvoice.endDate, payment.endDate)

        val monthlyPayment = createdSlot.captured.calculateMonthlyPayment()
        assertEquals(monthlyPayment, payment.paymentAmount, 0.0001)
    }

    @Test
    fun `do not attach payment info when schedule invoice provided but type is not loan`() {
        val repo = mockRepo()
        every { repo.existsByName(any()) } returns false
        val useCase = CreateProvisionable(repo)
        val input = CreateProvisionInput(
            title = "MacBook",
            initialCost = 1200.0,
            acquisitionDate = ACQUISITION_DATE,
            expectedLifespanMonth = 12,
            type = ProvisionType.DEPRECIATE,
            isPatrimony = false,
            depreciationCriteria = emptyList(),
            scheduleInvoice = buildScheduleInvoiceInput()
        )

        useCase.execAsync(input)

        val createdSlot = slot<Provision>()
        verify(exactly = 1) { repo.create(capture(createdSlot)) }
        assertNull(createdSlot.captured.paymentInfo)
    }
}

class GetProvisionTests {

    @Test
    fun `return mapped output for an existing provision`() {
        val repo = mockRepo()
        val provisionId = UUID.randomUUID()
        val provision = buildPlainProvision(id = provisionId)
        every { repo.get(provisionId) } returns provision
        val useCase = GetProvision(repo)

        val result = useCase.execAsync(provisionId)

        assertEquals(provisionId, result.id)
        assertEquals("MacBook", result.title)
        assertEquals(1200.0, result.initialCost)
        assertEquals(ACQUISITION_DATE, result.acquisitionDate)
        assertEquals(12, result.expectedLifespanMonth)
        assertEquals(1200.0, result.totalCost)
        assertEquals(50.0, result.costByMonth, 0.0001)
        assertEquals(0.0, result.monthlyPayment)
        assertEquals(provision.calculateResidualValue(), result.residualValue, 0.0001)
        assertNull(result.nextPaymentDate)
        assertNull(result.nextPaymentAmount)
    }

    @Test
    fun `return payment info for loan provision`() {
        val repo = mockRepo()
        val provisionId = UUID.randomUUID()
        val scheduler = Scheduler(LocalDateTime.of(2025, 3, 10, 0, 0))
        val payment = ProvisionPayment(
            accountId = UUID.randomUUID(),
            categoryId = UUID.randomUUID(),
            budgetIds = emptySet(),
            tagIds = emptySet(),
            paymentAmount = 1126.83,
            scheduler = scheduler,
            endDate = LocalDate.of(2026, 1, 1)
        )
        every { repo.get(provisionId) } returns buildLoanProvision(id = provisionId, paymentInfo = payment)
        val useCase = GetProvision(repo)

        val result = useCase.execAsync(provisionId)

        assertEquals(LocalDate.of(2025, 3, 10), result.nextPaymentDate)
        assertEquals(1126.83, result.nextPaymentAmount)
        assertEquals(13521.90, result.totalCost, 0.01)
        assertEquals(1126.825, result.monthlyPayment, 0.01)
    }

    @Test
    fun `throw when provision does not exist`() {
        val repo = mockRepo()
        val missingId = UUID.randomUUID()
        every { repo.get(missingId) } returns null
        val useCase = GetProvision(repo)

        assertThrows(DomainException.NotFound.Provisionable::class.java) {
            useCase.execAsync(missingId)
        }
    }
}

class GetAllProvisionableTests {

    @Test
    fun `map all provisions to outputs`() {
        val repo = mockRepo()
        val plain = buildPlainProvision()
        val payment = ProvisionPayment(
            accountId = UUID.randomUUID(),
            categoryId = UUID.randomUUID(),
            budgetIds = emptySet(),
            tagIds = emptySet(),
            paymentAmount = 1126.83,
            scheduler = Scheduler(LocalDateTime.of(2025, 3, 10, 0, 0)),
            endDate = LocalDate.of(2026, 1, 1)
        )
        val loan = buildLoanProvision(paymentInfo = payment)
        every { repo.getAll(any()) } returns RepoList(items = listOf(plain, loan), total = 2)
        val useCase = GetAllProvisionable(repo)

        val result = useCase.execAsync(QueryFilter.queryAll())

        assertEquals(2, result.total)
        assertEquals(2, result.items.size)

        val plainOutput = result.items.first { it.id == plain.id }
        assertEquals("MacBook", plainOutput.title)
        assertEquals(1200.0, plainOutput.totalCost)
        assertEquals(50.0, plainOutput.costByMonth, 0.0001)
        assertEquals(0.0, plainOutput.monthlyPayment)
        assertNull(plainOutput.nextPaymentDate)
        assertNull(plainOutput.nextPaymentAmount)

        val loanOutput = result.items.first { it.id == loan.id }
        assertEquals(13521.90, loanOutput.totalCost, 0.01)
        assertEquals(1126.825, loanOutput.monthlyPayment, 0.01)
        assertEquals(LocalDate.of(2025, 3, 10), loanOutput.nextPaymentDate)
        assertEquals(1126.83, loanOutput.nextPaymentAmount)
    }

    @Test
    fun `return empty list when no provisions`() {
        val repo = mockRepo()
        every { repo.getAll(any()) } returns RepoList(items = emptyList(), total = 0)
        val useCase = GetAllProvisionable(repo)

        val result = useCase.execAsync(QueryFilter.queryAll())

        assertEquals(0, result.total)
        assertTrue(result.items.isEmpty())
    }
}

class DeleteProvisionableTests {

    @Test
    fun `delete an existing provision`() {
        val repo = mockRepo()
        val provisionId = UUID.randomUUID()
        every { repo.get(provisionId) } returns buildPlainProvision(id = provisionId)
        val useCase = DeleteProvisionable(repo)

        useCase.execAsync(DeleteProvisionInput(provisionId))

        verify(exactly = 1) { repo.delete(provisionId) }
    }

    @Test
    fun `throw when deleting a missing provision`() {
        val repo = mockRepo()
        val missingId = UUID.randomUUID()
        every { repo.get(missingId) } returns null
        val useCase = DeleteProvisionable(repo)

        assertThrows(DomainException.NotFound.Provisionable::class.java) {
            useCase.execAsync(DeleteProvisionInput(missingId))
        }

        verify(exactly = 0) { repo.delete(any()) }
    }
}

class UpdateProvisionableTests {

    private fun baseInput(
        id: UUID,
        transform: (UpdateProvisionInput) -> UpdateProvisionInput = { it }
    ): UpdateProvisionInput {
        return transform(
            UpdateProvisionInput(
                id = id,
                title = null,
                initialCost = null,
                acquisitionDate = null,
                expectedLifespanMonth = null,
                isPatrimony = null,
                scheduleInvoice = null,
                depreciationCriteria = null,
                type = null,
                floorValue = null,
                interestLoan = null,
                loanMonth = null
            )
        )
    }

    @Test
    fun `update provided fields and persist`() {
        val repo = mockRepo()
        val provisionId = UUID.randomUUID()
        val provision = buildPlainProvision(id = provisionId)
        every { repo.get(provisionId) } returns provision
        every { repo.existsByName(any()) } returns false
        val useCase = UpdateProvisionable(buildUnitOfWork(), repo)
        val newDate = ACQUISITION_DATE.plusMonths(2)
        val newCriteria = listOf(
            ProvisionDepreciateCriteria("Half", "Half value", DepreciationType.FIX, 600.0),
            ProvisionDepreciateCriteria("Wear", "Wear", DepreciationType.STRAIGHT_LINE, 10.0)
        )

        useCase.execAsync(
            baseInput(provisionId) {
                it.copy(
                    title = "MacBook Pro",
                    initialCost = 2400.0,
                    acquisitionDate = newDate,
                    expectedLifespanMonth = 24,
                    isPatrimony = true,
                    floorValue = 300.0,
                    depreciationCriteria = newCriteria
                )
            }
        )

        val updatedSlot = slot<Provision>()
        verify(exactly = 1) { repo.update(capture(updatedSlot)) }

        val updated = updatedSlot.captured
        assertEquals("MacBook Pro", updated.title)
        assertEquals(2400.0, updated.initialCost)
        assertEquals(newDate, updated.acquisitionDate)
        assertEquals(24, updated.expectedLifespanMonth)
        assertTrue(updated.isPatrimony)
        assertEquals(300.0, updated.floorValue)
        assertEquals(2, updated.depreciationCriteria.size)
    }

    @Test
    fun `add and remove depreciation criteria by diffing with existing ones`() {
        val repo = mockRepo()
        val provisionId = UUID.randomUUID()
        val kept = ProvisionDepreciateCriteria("Kept", "Kept", DepreciationType.FIX, 100.0)
        val removed = ProvisionDepreciateCriteria("Removed", "Removed", DepreciationType.FIX, 200.0)
        val added = ProvisionDepreciateCriteria("Added", "Added", DepreciationType.STRAIGHT_LINE, 10.0)
        val provision = buildPlainProvision(
            id = provisionId,
            criteria = mutableListOf(kept, removed)
        )
        every { repo.get(provisionId) } returns provision
        val useCase = UpdateProvisionable(buildUnitOfWork(), repo)

        useCase.execAsync(baseInput(provisionId) {
            it.copy(floorValue = 500.0, depreciationCriteria = listOf(kept, added))
        })

        val updatedSlot = slot<Provision>()
        verify(exactly = 1) { repo.update(capture(updatedSlot)) }
        assertEquals(listOf(kept, added), updatedSlot.captured.depreciationCriteria)
    }

    @Test
    fun `recompute payment info when loan cost changes`() {
        val repo = mockRepo()
        val provisionId = UUID.randomUUID()
        val provision = buildLoanProvision(id = provisionId)
        every { repo.get(provisionId) } returns provision
        val useCase = UpdateProvisionable(buildUnitOfWork(), repo)
        val scheduleInvoice = buildScheduleInvoiceInput(
            scheduler = Scheduler(LocalDateTime.of(2025, 3, 10, 0, 0), SchedulerRecurrence(PeriodType.MONTH, 2))
        )

        useCase.execAsync(
            baseInput(provisionId) {
                it.copy(initialCost = 24000.0, type = ProvisionType.DEPRECIATE_LOAN, scheduleInvoice = scheduleInvoice)
            }
        )

        val updatedSlot = slot<Provision>()
        verify(exactly = 1) { repo.update(capture(updatedSlot)) }

        val updated = updatedSlot.captured
        assertEquals(24000.0, updated.initialCost)
        assertNotNull(updated.paymentInfo)
        assertEquals(scheduleInvoice.invoiceAccountId, updated.paymentInfo!!.accountId)

        val expectedMonthly = updated.calculateTotalCost() / 12
        assertEquals(expectedMonthly * 2, updated.paymentInfo!!.paymentAmount, 0.0001)
    }

    @Test
    fun `not persist when nothing changed`() {
        val repo = mockRepo()
        val provisionId = UUID.randomUUID()
        every { repo.get(provisionId) } returns buildPlainProvision(id = provisionId)
        val useCase = UpdateProvisionable(buildUnitOfWork(), repo)

        useCase.execAsync(baseInput(provisionId))

        verify(exactly = 0) { repo.update(any()) }
    }

    @Test
    fun `throw when renaming to a name that already exists`() {
        val repo = mockRepo()
        val provisionId = UUID.randomUUID()
        every { repo.get(provisionId) } returns buildPlainProvision(id = provisionId, title = "MacBook")
        every { repo.existsByName("MacBook") } returns true
        val useCase = UpdateProvisionable(buildUnitOfWork(), repo)

        assertThrows(DomainException.AlreadyExist.Provisionable::class.java) {
            useCase.execAsync(baseInput(provisionId) { it.copy(title = "MacBook") })
        }

        verify(exactly = 0) { repo.update(any()) }
    }

    @Test
    fun `throw when provision does not exist`() {
        val repo = mockRepo()
        val missingId = UUID.randomUUID()
        every { repo.get(missingId) } returns null
        val useCase = UpdateProvisionable(buildUnitOfWork(), repo)

        assertThrows(DomainException.NotFound.Provisionable::class.java) {
            useCase.execAsync(baseInput(missingId) { it.copy(title = "New") })
        }

        verify(exactly = 0) { repo.update(any()) }
    }
}

class ProvisionCommonTests {

    @Test
    fun `return initial cost when scheduler has no repeater`() {
        val scheduler = Scheduler(LocalDateTime.of(2025, 1, 1, 0, 0))

        val amount = ProvisionCommon.determineScheduleInvoiceDepreciateLoan(1000.0, 100.0, scheduler)

        assertEquals(1000.0, amount)
    }

    @Test
    fun `return initial cost when repeater interval is zero`() {
        val scheduler = Scheduler(LocalDateTime.of(2025, 1, 1, 0, 0), SchedulerRecurrence(PeriodType.MONTH, 0))

        val amount = ProvisionCommon.determineScheduleInvoiceDepreciateLoan(1000.0, 100.0, scheduler)

        assertEquals(1000.0, amount)
    }

    @Test
    fun `multiply monthly payment by interval for month period`() {
        val scheduler = Scheduler(LocalDateTime.of(2025, 1, 1, 0, 0), SchedulerRecurrence(PeriodType.MONTH, 3))

        val amount = ProvisionCommon.determineScheduleInvoiceDepreciateLoan(1000.0, 100.0, scheduler)

        assertEquals(300.0, amount, 0.0001)
    }

    @Test
    fun `multiply monthly payment by twelve times interval for year period`() {
        val scheduler = Scheduler(LocalDateTime.of(2025, 1, 1, 0, 0), SchedulerRecurrence(PeriodType.YEAR, 2))

        val amount = ProvisionCommon.determineScheduleInvoiceDepreciateLoan(1000.0, 100.0, scheduler)

        assertEquals(2400.0, amount, 0.0001)
    }

    @Test
    fun `divide monthly payment by four for week period`() {
        val scheduler = Scheduler(LocalDateTime.of(2025, 1, 1, 0, 0), SchedulerRecurrence(PeriodType.WEEK, 4))

        val amount = ProvisionCommon.determineScheduleInvoiceDepreciateLoan(1000.0, 100.0, scheduler)

        assertEquals(100.0, amount, 0.0001)
    }

    @Test
    fun `throw when recurrence period is day`() {
        val scheduler = Scheduler(LocalDateTime.of(2025, 1, 1, 0, 0), SchedulerRecurrence(PeriodType.DAY, 1))

        assertThrows(DomainException.BusinessLogic.ProvisionWithLoanMustHaveCantBeByDay::class.java) {
            ProvisionCommon.determineScheduleInvoiceDepreciateLoan(1000.0, 100.0, scheduler)
        }
    }
}