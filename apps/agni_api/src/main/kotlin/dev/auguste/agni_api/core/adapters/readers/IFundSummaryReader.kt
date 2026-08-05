package dev.auguste.agni_api.core.adapters.readers

import dev.auguste.agni_api.core.adapters.dto.FundSummaryOutput

interface IFundSummaryReader {
    fun getSummary(): FundSummaryOutput
}