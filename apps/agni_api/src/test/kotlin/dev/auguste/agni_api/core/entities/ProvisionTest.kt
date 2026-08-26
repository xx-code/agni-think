package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.entities.enums.DepreciationType
import dev.auguste.agni_api.core.entities.enums.ProvisionType
import dev.auguste.agni_api.core.value_objects.ProvisionDepreciateCriteria
import dev.auguste.agni_api.core.value_objects.ProvisionPayment
import dev.auguste.agni_api.core.value_objects.Scheduler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.pow

private val ACQUISITION_DATE: LocalDate = LocalDate.of(2024, 1, 1)

private fun buildPayment(
    paymentAmount: Double = 1126.83,
    endDate: LocalDate = ACQUISITION_DATE.plusMonths(12)
): ProvisionPayment {
    return ProvisionPayment(
        accountId = UUID.randomUUID(),
        categoryId = UUID.randomUUID(),
        budgetIds = emptySet(),
        tagIds = emptySet(),
        paymentAmount = paymentAmount,
        scheduler = Scheduler(LocalDateTime.of(2024, 2, 1, 0, 0)),
        endDate = endDate
    )
}

private fun buildProvision(
    title: String = "MacBook",
    initialCost: Double = 1000.0,
    isPatrimony: Boolean = false,
    acquisitionDate: LocalDate = ACQUISITION_DATE,
    expectedLifespanMonth: Int = 12,
    depreciationCriteria: MutableList<ProvisionDepreciateCriteria> = mutableListOf(),
    floorValue: Double = 0.0,
    type: ProvisionType = ProvisionType.DEPRECIATE,
    paymentInfo: ProvisionPayment? = null,
    interestLoan: Double = 0.0,
    loanMonth: Long = 0,
    id: UUID = UUID.randomUUID()
): Provision {
    return Provision(
        id = id,
        title = title,
        initialCost = initialCost,
        isPatrimony = isPatrimony,
        acquisitionDate = acquisitionDate,
        expectedLifespanMonth = expectedLifespanMonth,
        depreciationCriteria = depreciationCriteria,
        floorValue = floorValue,
        type = type,
        paymentInfo = paymentInfo,
        interestLoan = interestLoan,
        loanMonth = loanMonth
    )
}

class ProvisionTest {

    // ---------------------------------------------------------------
    // Initialization & defaults
    // ---------------------------------------------------------------

    @Test
    fun `initialize with default values`() {
        val provision = buildProvision()

        assertEquals("MacBook", provision.title)
        assertEquals(1000.0, provision.initialCost)
        assertEquals(ACQUISITION_DATE, provision.acquisitionDate)
        assertEquals(12, provision.expectedLifespanMonth)
        assertFalse(provision.isPatrimony)
        assertEquals(0.0, provision.floorValue)
        assertEquals(ProvisionType.DEPRECIATE, provision.type)
        assertEquals(0.0, provision.interestLoan)
        assertEquals(0, provision.loanMonth)
        assertNull(provision.paymentInfo)
        assertTrue(provision.depreciationCriteria.isEmpty())
    }

    @Test
    fun `initialize with provided values`() {
        val id = UUID.randomUUID()
        val payment = buildPayment()
        val criteria = mutableListOf(
            ProvisionDepreciateCriteria("Wear", "Standard wear", DepreciationType.STRAIGHT_LINE, 10.0)
        )

        val provision = buildProvision(
            title = "Voiture",
            initialCost = 20000.0,
            isPatrimony = true,
            acquisitionDate = ACQUISITION_DATE,
            expectedLifespanMonth = 120,
            depreciationCriteria = criteria,
            floorValue = 5000.0,
            type = ProvisionType.DEPRECIATE_LOAN,
            paymentInfo = payment,
            interestLoan = 4.5,
            loanMonth = 48,
            id = id
        )

        assertEquals(id, provision.id)
        assertEquals("Voiture", provision.title)
        assertEquals(20000.0, provision.initialCost)
        assertTrue(provision.isPatrimony)
        assertEquals(ACQUISITION_DATE, provision.acquisitionDate)
        assertEquals(120, provision.expectedLifespanMonth)
        assertEquals(criteria, provision.depreciationCriteria)
        assertEquals(5000.0, provision.floorValue)
        assertEquals(ProvisionType.DEPRECIATE_LOAN, provision.type)
        assertEquals(payment, provision.paymentInfo)
        assertEquals(4.5, provision.interestLoan)
        assertEquals(48, provision.loanMonth)
    }

    @Test
    fun `generate a unique id when not provided`() {
        val first = buildProvision()
        val second = buildProvision()

        assertNotEquals(first.id, second.id)
    }

    // ---------------------------------------------------------------
    // Change tracking (cleanObservable behaviour)
    // ---------------------------------------------------------------

    @Test
    fun `mark entity changed when a property is updated`() {
        val provision = buildProvision()
        assertFalse(provision.hasChanged())

        provision.title = "New title"
        assertTrue(provision.hasChanged())

        provision.resetChangeState()
        assertFalse(provision.hasChanged())

        provision.floorValue = 200.0
        assertTrue(provision.hasChanged())
    }

    @Test
    fun `do not mark entity changed when same value is assigned`() {
        val provision = buildProvision()

        provision.title = provision.title
        provision.floorValue = provision.floorValue

        assertFalse(provision.hasChanged())
    }

    // ---------------------------------------------------------------
    // Property validation
    // ---------------------------------------------------------------

    @Test
    fun `accept strictly positive initial cost`() {
        val provision = buildProvision()

        provision.initialCost = 1.0

        assertEquals(1.0, provision.initialCost)
    }

    @Test
    fun `reject zero initial cost`() {
        val provision = buildProvision()

        assertThrows(DomainException.BusinessLogic.Validation::class.java) {
            provision.initialCost = 0.0
        }
    }

    @Test
    fun `reject negative initial cost`() {
        val provision = buildProvision()

        assertThrows(DomainException.BusinessLogic.Validation::class.java) {
            provision.initialCost = -10.0
        }
    }

    @Test
    fun `accept zero interest loan`() {
        val provision = buildProvision(type = ProvisionType.DEPRECIATE_LOAN)

        provision.interestLoan = 0.0

        assertEquals(0.0, provision.interestLoan)
    }

    @Test
    fun `reject negative interest loan`() {
        val provision = buildProvision()

        val exception = assertThrows(DomainException.Validation.ProvisionDepreciateLoanInterestPositif::class.java) {
            provision.interestLoan = -1.0
        }

        assertEquals("PROVISION_DEPRECIATE_INTEREST_POSITIF", exception.code)
    }

    @Test
    fun `accept positive loan month for loan provision`() {
        val provision = buildProvision(type = ProvisionType.DEPRECIATE_LOAN)

        provision.loanMonth = 12

        assertEquals(12, provision.loanMonth)
    }

    @Test
    fun `reject loan month equal to zero for loan provision`() {
        val provision = buildProvision(type = ProvisionType.DEPRECIATE_LOAN)

        val exception = assertThrows(DomainException.Validation.ProvisionDepreciateLoanMonthMustBeGreaterThanZero::class.java) {
            provision.loanMonth = 0
        }

        assertEquals("PROVISION_DEPRECIATE_LOAN_MONTH_MUST_BE_GREATER_THAN_ZERO", exception.code)
    }

    @Test
    fun `reject negative loan month`() {
        val provision = buildProvision(type = ProvisionType.DEPRECIATE_LOAN)

        assertThrows(DomainException.Validation.ProvisionDepreciateLoanMonthMustBeGreaterThanZero::class.java) {
            provision.loanMonth = -6
        }
    }

    @Test
    fun `reject positive loan month on non loan provision`() {
        val provision = buildProvision(type = ProvisionType.DEPRECIATE)

        assertThrows(DomainException.Validation.ProvisionDepreciateLoanMonthMustBeGreaterThanZero::class.java) {
            provision.loanMonth = 12
        }
    }

    // ---------------------------------------------------------------
    // PaymentInfo validation
    // ---------------------------------------------------------------

    @Test
    fun `accept payment info for loan provision`() {
        val provision = buildProvision(type = ProvisionType.DEPRECIATE_LOAN)
        val payment = buildPayment()

        provision.paymentInfo = payment

        assertEquals(payment, provision.paymentInfo)
    }

    @Test
    fun `mark entity changed when payment info is assigned on loan provision`() {
        val provision = buildProvision(type = ProvisionType.DEPRECIATE_LOAN)

        provision.paymentInfo = buildPayment()

        assertTrue(provision.hasChanged())
    }

    @Test
    fun `reject null payment info assignment`() {
        val provision = buildProvision(type = ProvisionType.DEPRECIATE_LOAN)

        assertThrows(DomainException.BusinessLogic.ProvisionWithLoanMustHaveAScheduleInvoice::class.java) {
            provision.paymentInfo = null
        }
    }

    @Test
    fun `reject payment info on non loan provision`() {
        val provision = buildProvision(type = ProvisionType.DEPRECIATE)

        val exception = assertThrows(DomainException.BusinessLogic.ProvisionWithLoanMustHaveAScheduleInvoice::class.java) {
            provision.paymentInfo = buildPayment()
        }

        assertEquals("PROVISION_WITH_LOAN_MUST_HAVE_AS_SCHEDULE_INVOICE", exception.code)
    }

    // ---------------------------------------------------------------
    // calculateTotalCost
    // ---------------------------------------------------------------

    @Test
    fun `total cost is initial cost for non loan provision`() {
        val provision = buildProvision(
            initialCost = 1500.0,
            type = ProvisionType.DEPRECIATE,
            interestLoan = 10.0
        )

        assertEquals(1500.0, provision.calculateTotalCost())
    }

    @Test
    fun `total cost is initial cost for loan provision without interest`() {
        val provision = buildProvision(
            initialCost = 1500.0,
            type = ProvisionType.DEPRECIATE_LOAN,
            interestLoan = 0.0,
            loanMonth = 12
        )

        assertEquals(1500.0, provision.calculateTotalCost())
    }

    @Test
    fun `total cost is initial cost for loan provision without loan month`() {
        val provision = buildProvision(
            initialCost = 1500.0,
            type = ProvisionType.DEPRECIATE_LOAN,
            interestLoan = 5.0,
            loanMonth = 0
        )

        assertEquals(1500.0, provision.calculateTotalCost())
    }

    @Test
    fun `total cost applies compound monthly interest for loan provision`() {
        val provision = buildProvision(
            initialCost = 12000.0,
            type = ProvisionType.DEPRECIATE_LOAN,
            interestLoan = 12.0,
            loanMonth = 12
        )

        val expected = 12000.0 * (1.0 + (12.0 / 100.0) / 12.0).pow(12.0)

        assertEquals(expected, provision.calculateTotalCost(), 0.0001)
        assertEquals(13521.90, provision.calculateTotalCost(), 0.01)
    }

    // ---------------------------------------------------------------
    // calculateMonthlyPayment
    // ---------------------------------------------------------------

    @Test
    fun `monthly payment is zero when loan month is zero`() {
        val provision = buildProvision(initialCost = 1000.0)

        assertEquals(0.0, provision.calculateMonthlyPayment())
    }

    @Test
    fun `monthly payment divides total cost by loan month for loan provision`() {
        val provision = buildProvision(
            initialCost = 12000.0,
            type = ProvisionType.DEPRECIATE_LOAN,
            interestLoan = 12.0,
            loanMonth = 12
        )

        val expected = provision.calculateTotalCost() / 12

        assertEquals(expected, provision.calculateMonthlyPayment(), 0.0001)
        assertEquals(1126.825, provision.calculateMonthlyPayment(), 0.01)
    }

    @Test
    fun `monthly payment spreads total cost over loan month`() {
        val provision = buildProvision(
            initialCost = 2400.0,
            type = ProvisionType.DEPRECIATE,
            loanMonth = 24
        )

        assertEquals(100.0, provision.calculateMonthlyPayment(), 0.0001)
    }

    // ---------------------------------------------------------------
    // calculateTotalCostPerMonth
    // ---------------------------------------------------------------

    @Test
    fun `cost per month is zero when expected lifespan is zero`() {
        val provision = buildProvision(expectedLifespanMonth = 0)

        assertEquals(0.0, provision.calculateTotalCostPerMonth())
    }

    @Test
    fun `cost per month spreads depreciable amount over lifespan`() {
        val provision = buildProvision(
            initialCost = 1200.0,
            expectedLifespanMonth = 12,
            floorValue = 0.0,
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Half", "Half depreciated", DepreciationType.FIX, 600.0)
            )
        )

        assertEquals(50.0, provision.calculateTotalCostPerMonth(), 0.0001)
    }

    @Test
    fun `cost per month uses loan total cost for loan provision`() {
        val provision = buildProvision(
            initialCost = 12000.0,
            expectedLifespanMonth = 24,
            type = ProvisionType.DEPRECIATE_LOAN,
            interestLoan = 12.0,
            loanMonth = 12
        )

        val expected = (provision.calculateTotalCost() - provision.calculateResidualValue()) / 24

        assertEquals(expected, provision.calculateTotalCostPerMonth(), 0.0001)
    }

    @Test
    fun `cost per month is never negative when residual value exceeds total cost`() {
        val provision = buildProvision(
            initialCost = 1000.0,
            expectedLifespanMonth = 12,
            floorValue = 1500.0
        )

        assertEquals(0.0, provision.calculateTotalCostPerMonth())
    }

    // ---------------------------------------------------------------
    // calculateResidualValue
    // ---------------------------------------------------------------

    @Test
    fun `residual value equals initial cost when no criteria`() {
        val provision = buildProvision(initialCost = 1000.0)

        assertEquals(1000.0, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(10)), 0.0001)
    }

    @Test
    fun `reject declining balance criteria without month range`() {
        val exception = assertThrows(DomainException.Validation.ProvisionDepreciateCriteriaDecliningBalanceMustHaveRangeGreaterThanZero::class.java) {
            ProvisionDepreciateCriteria("Declining", "No range", DepreciationType.DECLINING_BALANCE, 24.0, 0)
        }

        assertEquals("PROVISION_DEPRECIATE_CRITERIA_DECLINING_BALANCE_MUST_HAVE_RANGE_GREATER_THAN_ZERO", exception.code)
    }

    @Test
    fun `accept declining balance criteria with positive month range`() {
        val criteria = ProvisionDepreciateCriteria("Declining", "With range", DepreciationType.DECLINING_BALANCE, 24.0, 12)

        assertEquals(12, criteria.monthRange)
    }

    @Test
    fun `residual value ignores criteria with zero or negative value`() {
        val provision = buildProvision(
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Zero", "No effect", DepreciationType.STRAIGHT_LINE, 0.0),
                ProvisionDepreciateCriteria("Negative", "No effect", DepreciationType.DECLINING_BALANCE, -10.0, 12),
                ProvisionDepreciateCriteria("Zero fix", "No effect", DepreciationType.FIX, 0.0)
            )
        )

        assertEquals(1000.0, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(5)), 0.0001)
    }

    @Test
    fun `residual value does not apply depreciation before ownership starts`() {
        val provision = buildProvision(
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Declining", "Declining", DepreciationType.DECLINING_BALANCE, 24.0, 12),
                ProvisionDepreciateCriteria("Straight", "10 percent per year", DepreciationType.STRAIGHT_LINE, 10.0)
            )
        )

        assertEquals(1000.0, provision.calculateResidualValue(ACQUISITION_DATE.minusMonths(3)), 0.0001)
        assertEquals(1000.0, provision.calculateResidualValue(ACQUISITION_DATE), 0.0001)
    }

    @Test
    fun `residual value applies declining balance for owned months`() {
        val provision = buildProvision(
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Declining", "24 percent per year", DepreciationType.DECLINING_BALANCE, 24.0, 12)
            )
        )

        val expected = 1000.0 * (1.0 - (24.0 / 100.0) / 12.0).pow(3.0)

        assertEquals(expected, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(3)), 0.0001)
        assertEquals(941.192, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(3)), 0.0001)
    }

    @Test
    fun `residual value caps declining balance at criteria month range`() {
        val provision = buildProvision(
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Declining", "24 percent per year", DepreciationType.DECLINING_BALANCE, 24.0, 6)
            )
        )

        val expected = 1000.0 * (1.0 - (24.0 / 100.0) / 12.0).pow(6.0)

        assertEquals(expected, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(24)), 0.0001)
        assertEquals(885.842380864, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(24)), 0.0001)
    }

    @Test
    fun `residual value consumes whole bracket when owned longer than its range`() {
        val provision = buildProvision(
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Declining", "24 percent per year", DepreciationType.DECLINING_BALANCE, 24.0, 12)
            )
        )

        val expected = 1000.0 * (1.0 - (24.0 / 100.0) / 12.0).pow(12.0)

        assertEquals(expected, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(24)), 0.0001)
        assertEquals(784.7167, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(24)), 0.01)
    }

    @Test
    fun `residual value applies each declining balance tranche to its own bracket`() {
        val provision = buildProvision(
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("First year", "24 percent per year", DepreciationType.DECLINING_BALANCE, 24.0, 12),
                ProvisionDepreciateCriteria("Second year", "12 percent per year", DepreciationType.DECLINING_BALANCE, 12.0, 24)
            )
        )

        val firstBracketMonths = 12.0
        val secondBracketMonths = 6.0
        val expected = 1000.0 *
                (1.0 - (24.0 / 100.0) / 12.0).pow(firstBracketMonths) *
                (1.0 - (12.0 / 100.0) / 12.0).pow(secondBracketMonths)

        assertEquals(expected, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(18)), 0.0001)
        assertEquals(738.7952, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(18)), 0.01)
    }

    @Test
    fun `residual value stops applying tranches beyond owned months`() {
        val provision = buildProvision(
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Half year", "24 percent per year", DepreciationType.DECLINING_BALANCE, 24.0, 6),
                ProvisionDepreciateCriteria("First year", "12 percent per year", DepreciationType.DECLINING_BALANCE, 12.0, 12),
                ProvisionDepreciateCriteria("Second year", "10 percent per year", DepreciationType.DECLINING_BALANCE, 10.0, 24)
            )
        )

        val firstBracketMonths = 6.0
        val secondBracketMonths = 3.0
        val expected = 1000.0 *
                (1.0 - (24.0 / 100.0) / 12.0).pow(firstBracketMonths) *
                (1.0 - (12.0 / 100.0) / 12.0).pow(secondBracketMonths)

        assertEquals(expected, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(9)), 0.0001)
        assertEquals(859.5320, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(9)), 0.01)
    }

    @Test
    fun `residual value applies straight line based on months owned`() {
        val provision = buildProvision(
            initialCost = 1200.0,
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Straight", "10 percent per year", DepreciationType.STRAIGHT_LINE, 10.0)
            )
        )

        val expected = 1200.0 - 1200.0 * ((10.0 / 100.0) / 12.0) * 6

        assertEquals(expected, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(6)), 0.0001)
        assertEquals(1140.0, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(6)), 0.0001)
    }

    @Test
    fun `residual value combines straight line criteria by summing their annual rates`() {
        val provision = buildProvision(
            initialCost = 1200.0,
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Second", "20 percent per year", DepreciationType.STRAIGHT_LINE, 20.0, 12),
                ProvisionDepreciateCriteria("First", "10 percent per year", DepreciationType.STRAIGHT_LINE, 10.0, 0)
            )
        )

        val expected = 1200.0 - 1200.0 * (((10.0 + 20.0) / 100.0) / 12.0) * 6

        assertEquals(expected, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(6)), 0.0001)
        assertEquals(1020.0, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(6)), 0.0001)
    }

    @Test
    fun `residual value applies declining balance before straight line deduction`() {
        val provision = buildProvision(
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Declining", "24 percent per year", DepreciationType.DECLINING_BALANCE, 24.0, 12),
                ProvisionDepreciateCriteria("Straight", "10 percent per year", DepreciationType.STRAIGHT_LINE, 10.0)
            )
        )

        val afterDecliningBalance = 1000.0 * (1.0 - (24.0 / 100.0) / 12.0).pow(6.0)
        val expected = afterDecliningBalance - 1000.0 * ((10.0 / 100.0) / 12.0) * 6

        assertEquals(expected, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(6)), 0.0001)
        assertEquals(835.8424, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(6)), 0.0001)
    }

    @Test
    fun `residual value subtracts fixed amounts`() {
        val provision = buildProvision(
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Battery", "Battery replacement", DepreciationType.FIX, 250.0),
                ProvisionDepreciateCriteria("Screen", "Screen wear", DepreciationType.FIX, 100.0)
            )
        )

        assertEquals(650.0, provision.calculateResidualValue(ACQUISITION_DATE.plusYears(2)), 0.0001)
    }

    @Test
    fun `residual value applies fixed percentage on current residual`() {
        val provision = buildProvision(
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Vat", "Vat deduction", DepreciationType.FIX_PERCENTAGE, 20.0),
                ProvisionDepreciateCriteria("Straight", "10 percent per year", DepreciationType.STRAIGHT_LINE, 10.0, 0)
            ),
            floorValue = 0.0
        )

        val afterStraightLine = 1000.0 - 1000.0 * ((10.0 / 100.0) / 12.0) * 6
        val expected = afterStraightLine - afterStraightLine * (20.0 / 100.0)

        assertEquals(expected, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(6)), 0.0001)
        assertEquals(760.0, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(6)), 0.0001)
    }

    @Test
    fun `residual value never goes below floor value`() {
        val provision = buildProvision(
            floorValue = 300.0,
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Big fix", "Almost all value", DepreciationType.FIX, 800.0)
            )
        )

        assertEquals(300.0, provision.calculateResidualValue(ACQUISITION_DATE.plusYears(2)), 0.0001)
    }

    @Test
    fun `residual value stays above floor value when depreciation is small`() {
        val provision = buildProvision(
            floorValue = 100.0,
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Small fix", "Little wear", DepreciationType.FIX, 100.0)
            )
        )

        assertEquals(900.0, provision.calculateResidualValue(ACQUISITION_DATE.plusYears(2)), 0.0001)
    }
}
