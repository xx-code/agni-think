package usecases.analystics

import adapters.dto.FundSummaryOutput
import adapters.readers.IFundSummaryReader
import usecases.interfaces.IUseCase

data class GetFundTotalSummary(
    private val fundSummaryReader: IFundSummaryReader,
): IUseCase<Unit, FundSummaryOutput> {
    override fun execAsync(input: Unit): FundSummaryOutput {
        val res = fundSummaryReader.getSummary()

        return res
    }
}
