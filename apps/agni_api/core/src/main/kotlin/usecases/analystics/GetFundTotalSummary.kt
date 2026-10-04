package usecases.analystics

import usecases.UseCase
import adapters.dto.FundSummaryOutput
import adapters.readers.IFundSummaryReader
import usecases.analystics.dto.FundSummaryInput

data class GetFundTotalSummary(
    private val fundSummaryReader: IFundSummaryReader,
): UseCase<FundSummaryInput, FundSummaryOutput>() {
    override suspend fun process(input: FundSummaryInput): FundSummaryOutput {
        val res = fundSummaryReader.getSummary(input.type)

        return res
    }
}
