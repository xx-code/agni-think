package dev.auguste.rest_api.controllers.models

import domain.enums.FundType
import domain.enums.PeriodType
import domain.value_objects.Scheduler
import domain.value_objects.SchedulerRecurrence
import domain.value_objects.SinkingFundDetail
import usecases.funds.dto.CreateFundInput
import usecases.funds.dto.UpdateFundInput
import java.time.LocalDate
import java.util.UUID


data class ApiUpgradeFundModel(
    val accountId: UUID,
    val amount: Double,
)

data class ApiFundSchedulerModel(
    val date: LocalDate,
    val repeater: ApiScheduleRepeaterModel?
)

data class ApiSinkingFundModel(
    val startDate: LocalDate,
    val numberOfContributions: Int = 1,
    val contributionRecurrence: ApiScheduleRepeaterModel,
    val scheduler: ApiFundSchedulerModel?,
)

data class ApiCreateFundModel(
    val title: String,
    val target: Double,
    val type: String,
    val description: String,
    val accountId: UUID?,
    val sinkingFundDetail: ApiSinkingFundModel?,
) {

    fun toCreateFundInput(): CreateFundInput {
        return CreateFundInput(
            target = target,
            title = title,
            description = description,
            type = FundType.fromString(type),
            accountId = accountId,
            sinkingFundDetail = sinkingFundDetail?.let {
                SinkingFundDetail(
                    startDate = it.startDate,
                    numberOfContributions = it.numberOfContributions,
                    contributionRecurrence = SchedulerRecurrence(
                        period = PeriodType.fromString(it.contributionRecurrence.period),
                        interval = it.contributionRecurrence.interval
                    ),
                    fundSchedule = it.scheduler?.let { sch ->
                        Scheduler(
                            date = sch.date.atStartOfDay(),
                            repeater = sch.repeater?.let {
                                SchedulerRecurrence(
                                    period = PeriodType.fromString(sch.repeater.period),
                                    interval = sch.repeater.interval
                                )
                            }
                        )
                    }
                )
            },
        )
    }
}

data class ApiUpdateFundModel(
    val title: String?,
    val target: Double?,
    val type: String?,
    val description: String?,
    val accountId: UUID?,
    val sinkingFundDetail: ApiSinkingFundModel?,
) {
    fun toUpdateFundInput(fundId: UUID): UpdateFundInput {
        return UpdateFundInput(
            id = fundId,
            target = target,
            title = title,
            description = description,
            type = type?.let { FundType.fromString(type)  },
            accountId = accountId,
            sinkingFundDetail = sinkingFundDetail?.let {
                SinkingFundDetail(
                    startDate = it.startDate,
                    numberOfContributions = it.numberOfContributions,
                    contributionRecurrence = SchedulerRecurrence(
                        period = PeriodType.fromString(it.contributionRecurrence.period),
                        interval = it.contributionRecurrence.interval
                    ),
                    fundSchedule = it.scheduler?.let { sch ->
                        Scheduler(
                            date = sch.date.atStartOfDay(),
                            repeater = sch.repeater?.let {
                                SchedulerRecurrence(
                                    period = PeriodType.fromString(sch.repeater.period),
                                    interval = sch.repeater.interval
                                )
                            }
                        )
                    }
                )
            },
        )
    }
}

data class ApiDeleteFundModel(
    val accountId: UUID?,
)
