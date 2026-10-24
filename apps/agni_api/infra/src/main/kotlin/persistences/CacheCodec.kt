package persistences

import domain.convertors.ICacheCodec
import domain.entities.*
import domain.enums.*
import domain.value_objects.*
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AccountCacheCodec : ICacheCodec<Account> {

    override fun encode(e: Account) = mapOf(
        "id" to e.id.toString(),
        "userId" to e.userId.toString(),
        "title" to e.title,
        "balance" to e.balance,
        "color" to e.color.toString(),
        "currencyId" to e.currencyId?.toString(),
        "detailType" to e.detail.getType().name,
        "detail" to e.detail.toMap(),
    )

    @Suppress("UNCHECKED_CAST")
    override fun decode(m: Map<String, Any?>): Account {
        val detailMap = m["detail"] as Map<String, Any>?
        val detail = when (AccountType.valueOf(m["detailType"] as String)) {
            AccountType.BROKING -> BrokingAccountDetail.fromMap(detailMap)
            AccountType.BUSINESS -> BusinessAccountDetail.fromMap(detailMap)
            AccountType.CHECKING -> CheckingAccountDetail.fromMap(detailMap)
            AccountType.CREDIT_CARD -> CreditCardAccountDetail.fromMap(detailMap)
            else -> error("Unsupported account type")
        }
        return Account(
            id = UUID.fromString(m["id"] as String),
            userId = UUID.fromString(m["userId"] as String),
            title = m["title"] as String,
            balance = (m["balance"] as Number).toDouble(),
            detail = detail,
            color = Color(m["color"] as String),
            currencyId = (m["currencyId"] as String?)?.let(UUID::fromString),
        )
    }
}