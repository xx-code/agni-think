package adapters.readers

import adapters.dto.FundSummaryOutput
import domain.enums.FundType

interface IFundSummaryReader {
    fun getSummary(type: FundType? = null): FundSummaryOutput
}