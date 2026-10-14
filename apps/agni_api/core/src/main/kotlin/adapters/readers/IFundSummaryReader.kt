package adapters.readers

import adapters.dto.FundSummaryOutput
import domain.enums.FundType
import java.util.UUID

interface IFundSummaryReader {
    fun getSummary(userId: UUID, type: FundType? = null): FundSummaryOutput
}