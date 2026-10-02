package adapters.readers

import adapters.dto.FundSummaryOutput

interface IFundSummaryReader {
    fun getSummary(): FundSummaryOutput
}