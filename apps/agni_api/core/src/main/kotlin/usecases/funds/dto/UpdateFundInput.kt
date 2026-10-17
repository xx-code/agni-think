package usecases.funds.dto

import domain.enums.FundType
import domain.value_objects.Scheduler
import domain.value_objects.SinkingFundDetail
import java.time.LocalDate
import java.util.UUID

data class UpdateFundInput(
    val id: UUID,
    val title: String?,
    val target: Double?,
    val description: String?,
    val type: FundType?,
    val accountId: UUID?,
    val sinkingFundDetail: SinkingFundDetail?
)
