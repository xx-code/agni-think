package usecases.analystics

import usecases.UseCase
import adapters.dto.QueryFilter
import domain.enums.PatrimonyType
import usecases.dto.ListOutput
import usecases.analystics.dto.GetPatrimonySummaryOutput
import usecases.interfaces.IUseCase
import usecases.patrimonies.dto.GetPatrimonyOutput
import kotlin.math.abs

class GetPatrimonySummary(
    private val getAllPatrimonies: IUseCase<QueryFilter, ListOutput<GetPatrimonyOutput>>
): UseCase<Unit, GetPatrimonySummaryOutput>() {
    override suspend fun process(input: Unit): GetPatrimonySummaryOutput {
        val patrimonies = getAllPatrimonies.processDirect(QueryFilter.queryAll())

        val totalAsset = patrimonies.items
                .filter { PatrimonyType.fromString(it.type) == PatrimonyType.ASSET }
                .sumOf { it.currentBalance }
        val totalLiability = patrimonies.items
                .filter { PatrimonyType.fromString(it.type) == PatrimonyType.LIABILITY }
                .sumOf { it.currentBalance }

        val totalPassAsset = patrimonies.items
                .filter { PatrimonyType.fromString(it.type) == PatrimonyType.ASSET }
                .sumOf { it.pastBalance }

        val totalPassLiability = patrimonies.items
                .filter { PatrimonyType.fromString(it.type) == PatrimonyType.LIABILITY }
                .sumOf { it.pastBalance }

        val networth = totalAsset - totalLiability
        val passNetworth = totalPassAsset - totalPassLiability

        val evolution = when {
            passNetworth == 0.0 -> if (networth > 0) 100.0 else 0.0
            else -> ((networth - passNetworth) / abs(passNetworth)) * 100.0
        }

        return GetPatrimonySummaryOutput(
            networth = networth,
            passNetworth = passNetworth,
            totalAsset = totalAsset,
            passTotalAsset = totalPassAsset,
            totalLiability = totalLiability,
            passTotalLiability = totalPassLiability,
            monthlyEvolutionPerc = evolution
        )
    }
}
