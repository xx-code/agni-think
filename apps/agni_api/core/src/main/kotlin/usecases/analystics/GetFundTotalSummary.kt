package usecases.analystics

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.dto.FundSummaryOutput
import adapters.readers.IFundSummaryReader
import adapters.repositories.IRepository
import domain.entities.User
import usecases.analystics.dto.FundSummaryInput

class GetFundTotalSummary(
    private val fundSummaryReader: IFundSummaryReader,
    auth: IAuthenticateAdapter,
): UseCase<FundSummaryInput, FundSummaryOutput>(auth = auth) {
    override suspend fun process(input: FundSummaryInput): FundSummaryOutput {
        val currentUser = getCurrentUser()
        val res = fundSummaryReader.getSummary(currentUser.id, input.type)

        return res
    }
}
