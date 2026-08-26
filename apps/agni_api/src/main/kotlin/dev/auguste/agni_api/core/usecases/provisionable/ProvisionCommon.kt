package dev.auguste.agni_api.core.usecases.provisionable

import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.value_objects.Scheduler

class ProvisionCommon {
    companion object {
        fun determineScheduleInvoiceDepreciateLoan(
            initialCost: Double,
            monthlyPayment: Double,
            scheduler: Scheduler
        ): Double {
            var amount =  initialCost
            if (scheduler.repeater != null) {
                val interval = scheduler.repeater.interval
                if (interval > 0) {
                    amount = when (scheduler.repeater.period) {
                        PeriodType.YEAR -> {
                            monthlyPayment*12*interval
                        }

                        PeriodType.MONTH -> {
                            monthlyPayment*interval
                        }

                        PeriodType.WEEK -> {
                            (monthlyPayment/4)*interval
                        }

                        PeriodType.DAY -> {
                            throw DomainException.BusinessLogic.ProvisionWithLoanMustHaveCantBeByDay()
                        }
                    }
                }
            }

            return amount
        }
    }
}