package dev.auguste.agni_api.core.adapters

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import java.time.LocalDate
import java.util.UUID

data class FinanceContextFund(
    val id: UUID,
    val balance: Double,
    val target: Double
)

interface IFinanceContext {
    fun getFund(id: UUID): FinanceContextFund
    fun verifyFundExists(id: UUID)
    fun getCategoryTotal(id: UUID, startDate: LocalDate, endDate: LocalDate): Double
    fun verifyCategoryExists(id: UUID)
    fun getNetWorthTotal(): Double
}

class FinanceContext(
    private val fundRepo: IRepository<SavingGoal>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    private val categoryRepo: IRepository<Category>
): IFinanceContext {
    override fun getFund(id: UUID): FinanceContextFund
    {
        val fund = fundRepo.get(id) ?: throw DomainException.NotFound.SavingGoal(id)
        return FinanceContextFund(
            id = id,
            balance = fund.balance,
            target = fund.target
        )
    }

    override fun verifyFundExists(id: UUID) {
        fundRepo.get(id) ?: throw DomainException.NotFound.SavingGoal(id)
    }

    override fun getCategoryTotal(id: UUID, startDate: LocalDate, endDate: LocalDate): Double {
        val balance = getBalance.execAsync(GetBalanceInput(
            categoryIds = setOf(id),
            startDate = startDate.atStartOfDay(),
            endDate = endDate.atStartOfDay()
        ))

        return balance.balance
    }

    override fun verifyCategoryExists(id: UUID) {
        categoryRepo.get(id) ?: throw DomainException.NotFound.Category(id)
    }

    override fun getNetWorthTotal(): Double {
        throw Exception("Net Worth total not supported")
    }
}