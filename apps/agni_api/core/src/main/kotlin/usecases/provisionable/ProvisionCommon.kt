package usecases.provisionable

import domain.exceptions.ValidationException
import domain.enums.PeriodType
import domain.value_objects.Scheduler

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
                        _root_ide_package_.domain.enums.PeriodType.YEAR -> {
                            monthlyPayment*12*interval
                        }

                        _root_ide_package_.domain.enums.PeriodType.MONTH -> {
                            monthlyPayment*interval
                        }

                        _root_ide_package_.domain.enums.PeriodType.WEEK -> {
                            (monthlyPayment/4)*interval
                        }

                        _root_ide_package_.domain.enums.PeriodType.DAY -> {
                            throw ValidationException.ProvisionWithLoanMustHaveCantBeByDay()
                        }
                    }
                }
            }

            return amount
        }
    }
}