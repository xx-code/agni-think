package usecases.analystics

import usecases.UseCase
import adapters.dto.FundSummaryOutput
import adapters.readers.IFundSummaryReader
data class GetFundTotalSummary(
    private val fundSummaryReader: IFundSummaryReader,
): UseCase<Unit, FundSummaryOutput>() {
    override suspend fun process(input: Unit): FundSummaryOutput {
        val res = fundSummaryReader.getSummary()

        return res
    }
}
