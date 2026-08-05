package dev.auguste.agni_api.core.usecases.analystics

import dev.auguste.agni_api.core.adapters.dto.FundSummaryOutput
import dev.auguste.agni_api.core.adapters.readers.IFundSummaryReader
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

data class GetFundTotalSummary(
    private val fundSummaryReader: IFundSummaryReader,
): IUseCase<Unit, FundSummaryOutput> {
    override fun execAsync(input: Unit): FundSummaryOutput {
        val res = fundSummaryReader.getSummary()

        return res
    }
}
