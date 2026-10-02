package usecases.analystics

import adapters.dto.QueryFilter
import domain.enums.PatrimonyType
import usecases.ListOutput
import usecases.analystics.dto.GetPatrimonySummaryOutput
import usecases.interfaces.IUseCase
import usecases.patrimonies.dto.GetPatrimonyOutput
import kotlin.math.abs

class GetPatrimonySummary(
    private val getAllPatrimonies: IUseCase<QueryFilter, ListOutput<GetPatrimonyOutput>>
): IUseCase<Unit, GetPatrimonySummaryOutput> {
    override fun execAsync(input: Unit): GetPatrimonySummaryOutput {
        val patrimonies = getAllPatrimonies.execAsync(QueryFilter.queryAll())

        val totalAsset = patrimonies.items
                .filter { _root_ide_package_.domain.enums.PatrimonyType.fromString(it.type) == _root_ide_package_.domain.enums.PatrimonyType.ASSET }
                .sumOf { it.currentBalance }
        val totalLiability = patrimonies.items
                .filter { _root_ide_package_.domain.enums.PatrimonyType.fromString(it.type) == _root_ide_package_.domain.enums.PatrimonyType.LIABILITY }
                .sumOf { it.currentBalance }

        val totalPassAsset = patrimonies.items
                .filter { _root_ide_package_.domain.enums.PatrimonyType.fromString(it.type) == _root_ide_package_.domain.enums.PatrimonyType.ASSET }
                .sumOf { it.pastBalance }

        val totalPassLiability = patrimonies.items
                .filter { _root_ide_package_.domain.enums.PatrimonyType.fromString(it.type) == _root_ide_package_.domain.enums.PatrimonyType.LIABILITY }
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
