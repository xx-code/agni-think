package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.adapters.FinanceContextFund
import dev.auguste.agni_api.core.adapters.IFinanceContext
import dev.auguste.agni_api.core.entities.enums.GoalEvaluationType
import dev.auguste.agni_api.core.entities.enums.GoalStatusType
import dev.auguste.agni_api.core.entities.factories.GoalEvaluationStrategyFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test

class FinanceContextMock(
    var funds: Map<UUID, Double>,
    var balanceByCategories: MutableMap<UUID, Double>,
    var netWorth: Double
): IFinanceContext {
    override fun getFund(id: UUID): FinanceContextFund {
        return FinanceContextFund(
            id = id,
            balance = funds.getValue(id),
            target = 2000.0
        )
    }

    override fun verifyFundExists(id: UUID) {
        println("verify funds $id")
    }

    override fun getCategoryTotal(
        id: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): Double {
        return balanceByCategories.getValue(id)
    }

    override fun verifyCategoryExists(id: UUID) {
        println("verify category $id")
    }

    override fun getNetWorthTotal(): Double {
        return netWorth
    }
}

class GoalTest {

    @Test
    fun `create a goal and evaluate`() {
        val fundId = UUID.randomUUID()
        val fundGoal = Goal(
            UUID.randomUUID(),
            "Fund Pipe",
            "Fond pour le pipe",
            fundId,
            targetAmount = 300.0,
            dueDate = LocalDate.now().plusDays(2),
            status = GoalStatusType.ACTIVE,
            type = GoalEvaluationType.FUND
        )

        val strategy = GoalEvaluationStrategyFactory.getStrategy(fundGoal.type)
        val financeContext = FinanceContextMock(
            mapOf(fundId to 100.0),
            mutableMapOf(),
            0.0
        )

        val evaluation = fundGoal.evaluateProgress(strategy, financeContext)

        val prc = (100.0/300.0)*100.0
        assertEquals(100.0, evaluation.balance)
        assertEquals(prc, evaluation.progressPercent)
    }

    @Test
    fun `evaluate a category target goal`() {
        val categoryId = UUID.randomUUID()
        val categoryGoal = Goal(
            UUID.randomUUID(),
            "Dépenses restaurant",
            "Limiter les dépenses restaurant",
            categoryId,
            targetAmount = 200.0,
            dueDate = LocalDate.now().plusDays(30),
            status = GoalStatusType.ACTIVE,
            type = GoalEvaluationType.TRANSACTION_TARGET
        )

        val strategy = GoalEvaluationStrategyFactory.getStrategy(categoryGoal.type)
        val financeContext = FinanceContextMock(
            mapOf(),
            mutableMapOf(categoryId to 50.0),
            0.0
        )

        val evaluation = categoryGoal.evaluateProgress(strategy, financeContext)

        assertEquals(50.0, evaluation.balance)
        assertEquals(25.0, evaluation.progressPercent)
    }

    @Test
    fun `progress is clamped at 100 when balance exceeds target`() {
        val fundId = UUID.randomUUID()
        val fundGoal = Goal(
            UUID.randomUUID(),
            "Fond d'urgence",
            "Fond pour les urgences",
            fundId,
            targetAmount = 300.0,
            dueDate = LocalDate.now().plusDays(2),
            status = GoalStatusType.ACTIVE,
            type = GoalEvaluationType.FUND
        )

        val strategy = GoalEvaluationStrategyFactory.getStrategy(fundGoal.type)
        val financeContext = FinanceContextMock(
            mapOf(fundId to 500.0),
            mutableMapOf(),
            0.0
        )

        val evaluation = fundGoal.evaluateProgress(strategy, financeContext)

        assertEquals(500.0, evaluation.balance)
        assertEquals(100.0, evaluation.progressPercent)
    }

    @Test
    fun `progress is zero when target amount is zero`() {
        val fundId = UUID.randomUUID()
        val fundGoal = Goal(
            UUID.randomUUID(),
            "Fond",
            "",
            fundId,
            targetAmount = 0.0,
            dueDate = LocalDate.now().plusDays(2),
            status = GoalStatusType.ACTIVE,
            type = GoalEvaluationType.FUND
        )

        val strategy = GoalEvaluationStrategyFactory.getStrategy(fundGoal.type)
        val financeContext = FinanceContextMock(
            mapOf(fundId to 100.0),
            mutableMapOf(),
            0.0
        )

        val evaluation = fundGoal.evaluateProgress(strategy, financeContext)

        assertEquals(100.0, evaluation.balance)
        assertEquals(0.0, evaluation.progressPercent)
    }

    @Test
    fun `factory returns the strategy registered for the type`() {
        assertTrue(GoalEvaluationStrategyFactory.getStrategy(GoalEvaluationType.FUND) is FundGoalEvaluationStrategy)
        assertTrue(GoalEvaluationStrategyFactory.getStrategy(GoalEvaluationType.TRANSACTION_TARGET) is CategoryEvaluationStrategy)
    }

    @Test
    fun `factory throws when no strategy is registered for the type`() {
        assertThrows(DomainException.BusinessLogic.GoalStrategyNotExist::class.java) {
            GoalEvaluationStrategyFactory.getStrategy(GoalEvaluationType.PATRIMONY)
        }
    }
}