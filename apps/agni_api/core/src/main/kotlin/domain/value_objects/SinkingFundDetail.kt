package domain.value_objects

import java.time.LocalDate

data class SinkingFundDetail(
    val startDate: LocalDate,
    val numberOfContributions: Int = 1,
    val contributionRecurrence: SchedulerRecurrence,
    val fundSchedule: Scheduler?
): IValueObject {
    override fun toMap(): Map<String, Any> {
        val map = mutableMapOf(
            "start_date" to startDate.toString(),
            "number_of_contributions" to numberOfContributions,
            "contribution_recurrence" to contributionRecurrence.toMap(),
        )

        if (fundSchedule != null) {
            map["fund_schedule"] = fundSchedule.toMap()
        }

        return map
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): SinkingFundDetail? {
            if (map == null) return null

            val startDateStr = map["start_date"] as? String ?: return null
            val numberOfContributions = (map["number_of_contributions"] as? Number)?.toInt() ?: 0

            val recurrenceMap = map["contribution_recurrence"] as? Map<String, Any>
            val contributionRecurrence = SchedulerRecurrence.fromMap(recurrenceMap)

            val fundScheduleMap = map["fund_schedule"] as? Map<String, Any>
            val fundSchedule = if (fundScheduleMap.isNullOrEmpty()) null else Scheduler.fromMap(fundScheduleMap)

            return SinkingFundDetail(
                startDate = LocalDate.parse(startDateStr),
                numberOfContributions = numberOfContributions,
                contributionRecurrence = contributionRecurrence,
                fundSchedule = fundSchedule
            )
        }
    }
}