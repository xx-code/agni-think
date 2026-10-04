package adapters

import adapters.repositories.IRepository
import domain.entities.Category
import domain.exceptions.NotFoundException
import domain.entities.Fund
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput
import java.time.LocalDate
import java.util.UUID

data class FinanceContextFund(
    val id: UUID,
    val balance: Double,
    val target: Double
)

interface IFinanceContext {
    suspend fun getFund(id: UUID): FinanceContextFund
    suspend fun verifyFundExists(id: UUID)
    suspend fun getCategoryTotal(id: UUID, startDate: LocalDate, endDate: LocalDate): Double
    suspend fun verifyCategoryExists(id: UUID)
    suspend fun getNetWorthTotal(): Double
}

class FinanceContext(
    private val fundRepo: IRepository<Fund>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    private val categoryRepo: IRepository<Category>
): IFinanceContext {
    override suspend fun getFund(id: UUID): FinanceContextFund
    {
        val fund = fundRepo.get(id) ?: throw NotFoundException.SingleEntity(id, "saving_goal")
        return FinanceContextFund(
            id = id,
            balance = fund.balance,
            target = fund.target
        )
    }

    override suspend fun verifyFundExists(id: UUID) {
        fundRepo.get(id) ?: throw NotFoundException.SingleEntity(id, "saving_goal")
    }

    override suspend fun getCategoryTotal(id: UUID, startDate: LocalDate, endDate: LocalDate): Double {
        val balance = getBalance.processDirect(
            GetBalanceInput(
                categoryIds = setOf(id),
                startDate = startDate.atStartOfDay(),
                endDate = endDate.atStartOfDay()
            )
        )

        return balance.balance
    }

    override suspend fun verifyCategoryExists(id: UUID) {
        categoryRepo.get(id) ?: throw NotFoundException.SingleEntity(id, "category")
    }

    override suspend fun getNetWorthTotal(): Double {
        throw Exception("Net Worth total not supported")
    }
}