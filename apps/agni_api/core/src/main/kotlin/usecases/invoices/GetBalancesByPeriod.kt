package usecases.invoices

import domain.exceptions.ValidationException
import domain.enums.PeriodType
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceByPeriodOutput
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput
import usecases.invoices.dto.GetBalancesByPeriodInput
import java.time.LocalDateTime

class GetBalancesByPeriod(
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
): IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>> {
    override fun execAsync(input: GetBalancesByPeriodInput): List<GetBalanceByPeriodOutput> {
        val results = mutableListOf<GetBalanceByPeriodOutput>()

        if (input.interval <= 0)
            throw ValidationException.IntervalCannotBeZero()

        var current = input.dateFrom
        val end = input.dateTo ?: LocalDateTime.now()

        while(current.isBefore(end)) {
            val next = when(input.period) {
                _root_ide_package_.domain.enums.PeriodType.YEAR -> current.plusYears(input.interval.toLong())
                _root_ide_package_.domain.enums.PeriodType.MONTH -> current.plusMonths(input.interval.toLong())
                _root_ide_package_.domain.enums.PeriodType.WEEK -> current.plusWeeks(input.interval.toLong())
                _root_ide_package_.domain.enums.PeriodType.DAY -> current.plusDays(input.interval.toLong())
            }

            val resBalance = getBalance.execAsync(GetBalanceInput(
                startDate = current,
                endDate = next,
                categoryIds = input.categoryIds,
                types = input.types,
                status = input.status,
                accountIds = input.accountIds,
                tagIds = input.tagIds,
                isFreeze = input.isFreeze,
                minAmount = input.minAmount,
                maxAmount = input.maxAmount,
                budgetIds = input.budgetIds,
                mouvement = input.mouvement,
                removeSystemCategory = input.removeSystemCategory
            ))

            results.add(
                GetBalanceByPeriodOutput(
                    date = current.toLocalDate(),
                    balance = resBalance.balance,
                    income = resBalance.income,
                    spend = resBalance.spend
                )
            )

            current = next
        }

        return results
    }
}