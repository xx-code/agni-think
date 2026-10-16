package domain.entities

import domain.enums.FundType
import domain.exceptions.ValidationException
import domain.value_objects.Scheduler
import domain.value_objects.SinkingFundDetail
import java.time.LocalDate
import java.util.UUID

class Fund(
    id: UUID = UUID.randomUUID(),
    val userId: UUID,
    title: String,
    description: String,
    target: Double,
    balance: Double,
    type: FundType,
    accountId: UUID?,
    sinkingFundDetail: SinkingFundDetail? = null,
): Entity(id = id) {

    var accountId by cleanObservable(accountId, this, {
        type == FundType.SINKING_FUND && it != null || type != FundType.SINKING_FUND
    }, {
        ValidationException.FundMustHaveAccountIdForThisType(type.value)
    })

    var title by cleanObservable(title, this)

    var description by cleanObservable(description, this)

    var target by cleanObservable(target, this, {
        it > 0
    }) {
        ValidationException.FundTargetAmountMustGreaterThanZero(it)
    }

    var balance by cleanObservable(balance, this)

    var type by cleanObservable(type, this)
    var sinkingFundDetail by cleanObservable(sinkingFundDetail, this, {
        it != null && type == FundType.SINKING_FUND
    }, {
        ValidationException.SinkingFundMustSetupDetail()
    })
}