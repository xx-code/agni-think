package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalancesByPeriodInput
import java.time.LocalDateTime

class GetBalancesByPeriod(
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
): IUseCase<GetBalancesByPeriodInput, List<GetBalanceOutput>> {
    override fun execAsync(input: GetBalancesByPeriodInput): List<GetBalanceOutput> {
        val results = mutableListOf<GetBalanceOutput>()

        if (input.interval <= 0)
            throw IllegalArgumentException("interval can not be zero")

        var current = input.dateFrom
        val end = input.dateTo ?: LocalDateTime.now()

        while(current.isBefore(end)) {
            val next = when(input.period) {
                PeriodType.YEAR -> current.plusYears(input.interval.toLong())
                PeriodType.MONTH -> current.plusMonths(input.interval.toLong())
                PeriodType.WEEK -> current.plusWeeks(input.interval.toLong())
                PeriodType.DAY -> current.plusDays(input.interval.toLong())
            }

            results.add(
                getBalance.execAsync(GetBalanceInput(
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
            )

            current = next
        }

        return results
    }
}