package domain.entities

import kotlinx.coroutines.runBlocking

import domain.exceptions.ValidationException

import domain.enums.DepreciationType
import domain.enums.ProvisionType
import domain.value_objects.ProvisionDepreciateCriteria
import domain.value_objects.ProvisionPayment
import domain.value_objects.Scheduler
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
    paymentAmount: Double = 1066.19,
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
    costHT: Double = 1000.0,
    costTTC: Double = 1200.0,
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
        costHT = costHT,
        costTTC = costTTC,
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
    fun `initialize with default values`() = runBlocking {
        val provision = buildProvision()

        assertEquals("MacBook", provision.title)
        assertEquals(1000.0, provision.costHT)
        assertEquals(1200.0, provision.costTTC)
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
    fun `initialize with provided values`() = runBlocking {
        val id = UUID.randomUUID()
        val payment = buildPayment()
        val criteria = mutableListOf(
            ProvisionDepreciateCriteria("Wear", "Standard wear", DepreciationType.STRAIGHT_LINE, 10.0)
        )

        val provision = buildProvision(
            title = "Voiture",
            costHT = 18000.0,
            costTTC = 20000.0,
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
        assertEquals(18000.0, provision.costHT)
        assertEquals(20000.0, provision.costTTC)
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
    fun `generate a unique id when not provided`() = runBlocking {
        val first = buildProvision()
        val second = buildProvision()

        assertNotEquals(first.id, second.id)
    }

    // ---------------------------------------------------------------
    // Change tracking (cleanObservable behaviour)
    // ---------------------------------------------------------------

    @Test
    fun `mark entity changed when a property is updated`() = runBlocking {
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
    fun `do not mark entity changed when same value is assigned`() = runBlocking {
        val provision = buildProvision()

        provision.title = provision.title
        provision.floorValue = provision.floorValue

        assertFalse(provision.hasChanged())
    }

    // ---------------------------------------------------------------
    // Property validation
    // ---------------------------------------------------------------

    @Test
    fun `accept strictly positive costHT and costTTC`() = runBlocking {
        val provision = buildProvision()

        provision.costHT = 1.0
        provision.costTTC = 1.0

        assertEquals(1.0, provision.costHT)
        assertEquals(1.0, provision.costTTC)
    }

    @Test
    fun `reject zero or negative costHT`() = runBlocking {
        val provision = buildProvision()

        assertThrows(ValidationException::class.java) {
            provision.costHT = 0.0
        }

        assertThrows(ValidationException::class.java) {
            provision.costHT = -10.0
        }
    }

    @Test
    fun `reject zero or negative costTTC`() = runBlocking {
        val provision = buildProvision()

        assertThrows(ValidationException::class.java) {
            provision.costTTC = 0.0
        }

        assertThrows(ValidationException::class.java) {
            provision.costTTC = -10.0
        }
    }

    @Test
    fun `accept zero interest loan`() = runBlocking {
        val provision = buildProvision(type = ProvisionType.DEPRECIATE_LOAN)

        provision.interestLoan = 0.0

        assertEquals(0.0, provision.interestLoan)
    }

    @Test
    fun `reject negative interest loan`() = runBlocking {
        val provision = buildProvision()

        val exception = assertThrows(ValidationException::class.java) {
            provision.interestLoan = -1.0
        }

        assertEquals("PROVISION_DEPRECIATE_INTEREST_POSITIF", exception.errorKey)
    }

    @Test
    fun `accept positive loan month for loan provision`() = runBlocking {
        val provision = buildProvision(type = ProvisionType.DEPRECIATE_LOAN)

        provision.loanMonth = 12

        assertEquals(12, provision.loanMonth)
    }

    @Test
    fun `reject loan month equal to zero for loan provision`() = runBlocking {
        val provision = buildProvision(type = ProvisionType.DEPRECIATE_LOAN)

        val exception = assertThrows(ValidationException::class.java) {
            provision.loanMonth = 0
        }

        assertEquals("PROVISION_DEPRECIATE_LOAN_MONTH_MUST_BE_GREATER_THAN_ZERO", exception.errorKey)
    }

    @Test
    fun `reject negative loan month`() = runBlocking {
        val provision = buildProvision(type = ProvisionType.DEPRECIATE_LOAN)

        assertThrows(ValidationException::class.java) {
            provision.loanMonth = -6
        }
    }

    @Test
    fun `reject positive loan month on non loan provision`() = runBlocking {
        val provision = buildProvision(type = ProvisionType.DEPRECIATE)

        assertThrows(ValidationException::class.java) {
            provision.loanMonth = 12
        }
    }

    // ---------------------------------------------------------------
    // PaymentInfo validation
    // ---------------------------------------------------------------

    @Test
    fun `accept payment info for loan provision`() = runBlocking {
        val provision = buildProvision(type = ProvisionType.DEPRECIATE_LOAN)
        val payment = buildPayment()

        provision.paymentInfo = payment

        assertEquals(payment, provision.paymentInfo)
    }

    @Test
    fun `mark entity changed when payment info is assigned on loan provision`() = runBlocking {
        val provision = buildProvision(type = ProvisionType.DEPRECIATE_LOAN)

        provision.paymentInfo = buildPayment()

        assertTrue(provision.hasChanged())
    }

    @Test
    fun `reject null payment info assignment`() = runBlocking {
        val provision = buildProvision(type = ProvisionType.DEPRECIATE_LOAN)

        assertThrows(ValidationException::class.java) {
            provision.paymentInfo = null
        }
    }

    @Test
    fun `reject payment info on non loan provision`() = runBlocking {
        val provision = buildProvision(type = ProvisionType.DEPRECIATE)

        val exception = assertThrows(ValidationException::class.java) {
            provision.paymentInfo = buildPayment()
        }

        assertEquals("PROVISION_WITH_LOAN_MUST_HAVE_AS_SCHEDULE_INVOICE", exception.errorKey)
    }

    // ---------------------------------------------------------------
    // calculateMonthlyPayment
    // ---------------------------------------------------------------

    @Test
    fun `monthly payment for non loan provision divides costTTC by loan month`() = runBlocking {
        val provision = buildProvision(
            costTTC = 12000.0,
            type = ProvisionType.DEPRECIATE,
            loanMonth = 12
        )

        assertEquals(1000.0, provision.calculateMonthlyPayment(), 0.0001)
    }

    @Test
    fun `monthly payment for non loan provision falls back on loan month one`() = runBlocking {
        val provision = buildProvision(
            costTTC = 500.0,
            type = ProvisionType.DEPRECIATE,
            loanMonth = 0
        )

        assertEquals(500.0, provision.calculateMonthlyPayment(), 0.0001)
    }

    @Test
    fun `monthly payment for loan provision without interest spreads costTTC`() = runBlocking {
        val provision = buildProvision(
            costTTC = 12000.0,
            type = ProvisionType.DEPRECIATE_LOAN,
            interestLoan = 0.0,
            loanMonth = 12
        )

        assertEquals(1000.0, provision.calculateMonthlyPayment(), 0.0001)
    }

    @Test
    fun `monthly payment for loan provision uses amortization formula`() = runBlocking {
        val provision = buildProvision(
            costTTC = 12000.0,
            type = ProvisionType.DEPRECIATE_LOAN,
            interestLoan = 12.0,
            loanMonth = 12
        )

        val monthlyRate = (12.0 / 100.0) / 12.0
        val expected = 12000.0 * monthlyRate / (1.0 - (1.0 + monthlyRate).pow(-12.0))

        assertEquals(expected, provision.calculateMonthlyPayment(), 0.0001)
        assertEquals(1066.19, provision.calculateMonthlyPayment(), 0.01)
    }

    // ---------------------------------------------------------------
    // calculateTotalCost
    // ---------------------------------------------------------------

    @Test
    fun `total cost is costHT for non loan provision`() = runBlocking {
        val provision = buildProvision(
            costHT = 1500.0,
            costTTC = 1650.0,
            type = ProvisionType.DEPRECIATE,
            interestLoan = 10.0,
            loanMonth = 12
        )

        assertEquals(1500.0, provision.calculateTotalCost(), 0.0001)
    }

    @Test
    fun `total cost is costTTC for loan provision without interest`() = runBlocking {
        val provision = buildProvision(
            costHT = 1000.0,
            costTTC = 1200.0,
            type = ProvisionType.DEPRECIATE_LOAN,
            interestLoan = 0.0,
            loanMonth = 12
        )

        assertEquals(1200.0, provision.calculateTotalCost(), 0.0001)
    }

    @Test
    fun `total cost is costTTC for loan provision without loan month`() = runBlocking {
        val provision = buildProvision(
            costHT = 1000.0,
            costTTC = 1200.0,
            type = ProvisionType.DEPRECIATE_LOAN,
            interestLoan = 5.0,
            loanMonth = 0
        )

        assertEquals(1200.0, provision.calculateTotalCost(), 0.0001)
    }

    @Test
    fun `total cost for loan provision is amortized payment times months`() = runBlocking {
        val provision = buildProvision(
            costTTC = 12000.0,
            type = ProvisionType.DEPRECIATE_LOAN,
            interestLoan = 12.0,
            loanMonth = 12
        )

        val expected = provision.calculateMonthlyPayment() * 12

        assertEquals(expected, provision.calculateTotalCost(), 0.0001)
        assertEquals(12794.23, provision.calculateTotalCost(), 0.01)
    }

    // ---------------------------------------------------------------
    // calculateTotalCostPerMonth
    // ---------------------------------------------------------------

    @Test
    fun `cost per month is zero when expected lifespan is zero`() = runBlocking {
        val provision = buildProvision(expectedLifespanMonth = 0)

        assertEquals(0.0, provision.calculateTotalCostPerMonth())
    }

    @Test
    fun `cost per month spreads depreciable amount over lifespan`() = runBlocking {
        val provision = buildProvision(
            costHT = 1200.0,
            costTTC = 1200.0,
            expectedLifespanMonth = 12,
            floorValue = 0.0,
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Half", "Half depreciated", DepreciationType.FIX, 600.0)
            )
        )

        assertEquals(50.0, provision.calculateTotalCostPerMonth(), 0.0001)
    }

    @Test
    fun `cost per month uses loan total cost for loan provision`() = runBlocking {
        val provision = buildProvision(
            costTTC = 12000.0,
            expectedLifespanMonth = 24,
            type = ProvisionType.DEPRECIATE_LOAN,
            interestLoan = 12.0,
            loanMonth = 12
        )

        val expected = (provision.calculateTotalCost() - provision.calculateResidualValue()) / 24

        assertEquals(expected, provision.calculateTotalCostPerMonth(), 0.0001)
    }

    @Test
    fun `cost per month is never negative when residual value exceeds total cost`() = runBlocking {
        val provision = buildProvision(
            costHT = 1000.0,
            costTTC = 1000.0,
            expectedLifespanMonth = 12,
            floorValue = 1500.0
        )

        assertEquals(0.0, provision.calculateTotalCostPerMonth())
    }

    // ---------------------------------------------------------------
    // calculateResidualValue
    // ---------------------------------------------------------------

    @Test
    fun `residual value equals costHT when no criteria`() = runBlocking {
        val provision = buildProvision(costHT = 1000.0)

        assertEquals(1000.0, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(10)), 0.0001)
    }

    @Test
    fun `reject declining balance criteria without month range`() = runBlocking {
        val exception = assertThrows(ValidationException::class.java) {
            ProvisionDepreciateCriteria("Declining", "No range", DepreciationType.DECLINING_BALANCE, 24.0, 0)
        }

        assertEquals("PROVISION_DEPRECIATE_CRITERIA_DECLINING_BALANCE_MUST_HAVE_RANGE_GREATER_THAN_ZERO", exception.errorKey)
    }

    @Test
    fun `accept declining balance criteria with positive month range`() = runBlocking {
        val criteria = ProvisionDepreciateCriteria("Declining", "With range", DepreciationType.DECLINING_BALANCE, 24.0, 12)

        assertEquals(12, criteria.monthRange)
    }

    @Test
    fun `residual value ignores criteria with zero or negative value`() = runBlocking {
        val provision = buildProvision(
            costHT = 1000.0,
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Zero", "No effect", DepreciationType.STRAIGHT_LINE, 0.0),
                ProvisionDepreciateCriteria("Negative", "No effect", DepreciationType.DECLINING_BALANCE, -10.0, 12),
                ProvisionDepreciateCriteria("Zero fix", "No effect", DepreciationType.FIX, 0.0)
            )
        )

        assertEquals(1000.0, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(5)), 0.0001)
    }

    @Test
    fun `residual value does not apply depreciation before ownership starts`() = runBlocking {
        val provision = buildProvision(
            costHT = 1000.0,
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Declining", "Declining", DepreciationType.DECLINING_BALANCE, 24.0, 12),
                ProvisionDepreciateCriteria("Straight", "10 percent per year", DepreciationType.STRAIGHT_LINE, 10.0)
            )
        )

        assertEquals(1000.0, provision.calculateResidualValue(ACQUISITION_DATE.minusMonths(3)), 0.0001)
        assertEquals(1000.0, provision.calculateResidualValue(ACQUISITION_DATE), 0.0001)
    }

    @Test
    fun `residual value applies declining balance for owned months`() = runBlocking {
        val provision = buildProvision(
            costHT = 1000.0,
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Declining", "24 percent per year", DepreciationType.DECLINING_BALANCE, 24.0, 12)
            )
        )

        val expected = 1000.0 * (1.0 - (24.0 / 100.0) / 12.0).pow(3.0)

        assertEquals(expected, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(3)), 0.0001)
        assertEquals(941.192, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(3)), 0.0001)
    }

    @Test
    fun `residual value caps declining balance at criteria month range`() = runBlocking {
        val provision = buildProvision(
            costHT = 1000.0,
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Declining", "24 percent per year", DepreciationType.DECLINING_BALANCE, 24.0, 6)
            )
        )

        val expected = 1000.0 * (1.0 - (24.0 / 100.0) / 12.0).pow(6.0)

        assertEquals(expected, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(24)), 0.0001)
        assertEquals(885.842380864, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(24)), 0.0001)
    }

    @Test
    fun `residual value consumes whole bracket when owned longer than its range`() = runBlocking {
        val provision = buildProvision(
            costHT = 1000.0,
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Declining", "24 percent per year", DepreciationType.DECLINING_BALANCE, 24.0, 12)
            )
        )

        val expected = 1000.0 * (1.0 - (24.0 / 100.0) / 12.0).pow(12.0)

        assertEquals(expected, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(24)), 0.0001)
        assertEquals(784.7167, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(24)), 0.01)
    }

    @Test
    fun `residual value applies each declining balance tranche to its own bracket`() = runBlocking {
        val provision = buildProvision(
            costHT = 1000.0,
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
    fun `residual value stops applying tranches beyond owned months`() = runBlocking {
        val provision = buildProvision(
            costHT = 1000.0,
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
    fun `residual value applies straight line based on months owned`() = runBlocking {
        val provision = buildProvision(
            costHT = 1200.0,
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Straight", "10 percent per year", DepreciationType.STRAIGHT_LINE, 10.0)
            )
        )

        val expected = 1200.0 - 1200.0 * ((10.0 / 100.0) / 12.0) * 6

        assertEquals(expected, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(6)), 0.0001)
        assertEquals(1140.0, provision.calculateResidualValue(ACQUISITION_DATE.plusMonths(6)), 0.0001)
    }

    @Test
    fun `residual value combines straight line criteria by summing their annual rates`() = runBlocking {
        val provision = buildProvision(
            costHT = 1200.0,
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
    fun `residual value applies declining balance before straight line deduction`() = runBlocking {
        val provision = buildProvision(
            costHT = 1000.0,
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
    fun `residual value subtracts fixed amounts`() = runBlocking {
        val provision = buildProvision(
            costHT = 1000.0,
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Battery", "Battery replacement", DepreciationType.FIX, 250.0),
                ProvisionDepreciateCriteria("Screen", "Screen wear", DepreciationType.FIX, 100.0)
            )
        )

        assertEquals(650.0, provision.calculateResidualValue(ACQUISITION_DATE.plusYears(2)), 0.0001)
    }

    @Test
    fun `residual value applies fixed percentage on current residual`() = runBlocking {
        val provision = buildProvision(
            costHT = 1000.0,
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
    fun `residual value never goes below floor value`() = runBlocking {
        val provision = buildProvision(
            costHT = 1000.0,
            floorValue = 300.0,
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Big fix", "Almost all value", DepreciationType.FIX, 800.0)
            )
        )

        assertEquals(300.0, provision.calculateResidualValue(ACQUISITION_DATE.plusYears(2)), 0.0001)
    }

    @Test
    fun `residual value stays above floor value when depreciation is small`() = runBlocking {
        val provision = buildProvision(
            costHT = 1000.0,
            floorValue = 100.0,
            depreciationCriteria = mutableListOf(
                ProvisionDepreciateCriteria("Small fix", "Little wear", DepreciationType.FIX, 100.0)
            )
        )

        assertEquals(900.0, provision.calculateResidualValue(ACQUISITION_DATE.plusYears(2)), 0.0001)
    }
}
