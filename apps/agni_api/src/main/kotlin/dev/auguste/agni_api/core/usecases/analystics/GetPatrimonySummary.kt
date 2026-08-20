package dev.auguste.agni_api.core.usecases.analystics

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.entities.enums.PatrimonyType
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetPatrimonySummaryOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.patrimonies.dto.GetPatrimonyOutput
import kotlin.math.abs

class GetPatrimonySummary(
    private val getAllPatrimonies: IUseCase<QueryFilter, ListOutput<GetPatrimonyOutput>>
): IUseCase<Unit, GetPatrimonySummaryOutput> {
    override fun execAsync(input: Unit): GetPatrimonySummaryOutput {
        val patrimonies = getAllPatrimonies.execAsync(QueryFilter.queryAll())

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
