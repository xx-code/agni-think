package usecases.funds.dto

import adapters.dto.ScheduleRepeaterOutput
import java.time.LocalDate
import java.util.UUID

data class FundGoalOutput(
    val id: UUID,
    val title: String,
    val dueDate: LocalDate
)

data class FundScheduleOutput(
    val date: LocalDate,
    val repeater: ScheduleRepeaterOutput?
)

data class SinkingFundDetailOutput(
    val numberOfContributions: Int,
    val startDate: LocalDate,
    val contributionRecurrence: ScheduleRepeaterOutput,
    val fundScheduler: FundScheduleOutput?,
    val isClosed: Boolean
)

data class GetFundOutput(
    val id: UUID,
    val title: String,
    val description: String,
    val type: String,
    val target: Double,
    val balance: Double,
    val accountId: UUID?,
    val goals: List<FundGoalOutput>,
    val sinkingFundDetail: SinkingFundDetailOutput?
)
