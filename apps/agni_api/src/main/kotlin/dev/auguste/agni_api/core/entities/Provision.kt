package dev.auguste.agni_api.core.entities

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.properties.Delegates

class Provision(
    id: UUID = UUID.randomUUID(),
    title: String,
    initialCost: Double,
    acquisitionDate: LocalDate,
    expectedLifespanMonth: Int,
    residualValue: Double = 0.0,
    ): Entity(id = id) {

    var title by Delegates.observable(title) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var initialCost by Delegates.observable(initialCost) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var acquisitionDate by Delegates.observable(acquisitionDate) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var expectedLifespanMonth by Delegates.observable(expectedLifespanMonth) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var residualValue by Delegates.observable(residualValue) { prop, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    fun calculateCurrentValue(): Double {
        val monthsOwned = ChronoUnit.MONTHS.between(acquisitionDate, LocalDate.now())
        if (monthsOwned >= expectedLifespanMonth)
            return 0.0

        val totalDepreciation = initialCost - residualValue
        val monthlyDepeciation = totalDepreciation / expectedLifespanMonth

        return initialCost - (monthlyDepeciation * monthsOwned)
    }

    fun getMonthlyProvisionTarget(): Double {
        return initialCost / expectedLifespanMonth
    }
}