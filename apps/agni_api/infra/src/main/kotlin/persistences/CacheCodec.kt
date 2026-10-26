package persistences

import domain.convertors.ICacheCodec
import domain.entities.*
import domain.enums.*
import domain.value_objects.*
import org.springframework.stereotype.Component
import java.time.LocalDate
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

@Component
class CurrencyCacheCodec : ICacheCodec<Currency> {
    override fun encode(e: Currency) = mapOf(
        "id" to e.id.toString(),
        "name" to e.name,
        "symbol" to e.symbol,
        "locale" to e.locale,
        "rateToBase" to e.rateToBase,
        "isBase" to e.isBase,
    )

    override fun decode(m: Map<String, Any?>): Currency {
        return Currency(
            id = UUID.fromString(m["id"] as String),
            name = m["name"] as String,
            symbol = m["symbol"] as String,
            locale = m["locale"] as String?,
            rateToBase = (m["rateToBase"] as Number?)?.toDouble(),
            isBase = m["isBase"] as Boolean,
        )
    }
}

@Component
class CategoryCacheCodec : ICacheCodec<Category> {
    override fun encode(e: Category) = mapOf(
        "id" to e.id.toString(),
        "title" to e.title,
        "icon" to e.icon,
        "color" to e.color.toString(),
        "userId" to e.userId?.toString(),
        "isSystem" to e.isSystem,
        "isArchived" to e.isArchived,
    )

    override fun decode(m: Map<String, Any?>): Category {
        return Category(
            id = UUID.fromString(m["id"] as String),
            title = m["title"] as String,
            icon = m["icon"] as String,
            color = Color(m["color"] as String),
            userId = (m["userId"] as String?)?.let(UUID::fromString),
            isSystem = m["isSystem"] as Boolean,
            isArchived = m["isArchived"] as Boolean,
        )
    }
}

@Component
class TagCacheCodec : ICacheCodec<Tag> {
    override fun encode(e: Tag) = mapOf(
        "id" to e.id.toString(),
        "value" to e.value,
        "color" to e.color.toString(),
        "userId" to e.userId?.toString(),
        "isSystem" to e.isSystem,
        "isArchived" to e.isArchived,
    )

    override fun decode(m: Map<String, Any?>): Tag {
        return Tag(
            id = UUID.fromString(m["id"] as String),
            value = m["value"] as String,
            color = Color(m["color"] as String),
            userId = (m["userId"] as String?)?.let(UUID::fromString),
            isSystem = m["isSystem"] as Boolean,
            isArchived = m["isArchived"] as Boolean,
        )
    }
}

@Component
class FinancePrincipleCacheCodec : ICacheCodec<FinancePrinciple> {
    override fun encode(e: FinancePrinciple) = mapOf(
        "id" to e.id.toString(),
        "userId" to e.userId.toString(),
        "name" to e.name,
        "description" to e.description,
        "targetType" to e.targetType.name,
        "strictness" to e.strictness,
        "logicRules" to e.logicRules,
    )

    override fun decode(m: Map<String, Any?>): FinancePrinciple {
        return FinancePrinciple(
            id = UUID.fromString(m["id"] as String),
            userId = UUID.fromString(m["userId"] as String),
            name = m["name"] as String,
            description = m["description"] as String,
            targetType = PrincipleType.valueOf(m["targetType"] as String),
            strictness = (m["strictness"] as Number).toInt(),
            logicRules = m["logicRules"] as String?,
        )
    }
}

@Component
class SpendingPeriodTemplateCacheCodec : ICacheCodec<SpendingPeriodTemplate> {
    @Suppress("UNCHECKED_CAST")
    override fun encode(e: SpendingPeriodTemplate) = mapOf(
        "id" to e.id.toString(),
        "userId" to e.userId.toString(),
        "startDate" to e.startDate.toString(),
        "recurrence" to e.recurrence.toMap(),
        "targetBudgetIds" to e.targetBudgetIds.map { it.toString() }.toSet(),
        "isActive" to e.isActive,
        "endDate" to e.endDate?.toString(),
    )

    @Suppress("UNCHECKED_CAST")
    override fun decode(m: Map<String, Any?>): SpendingPeriodTemplate {
        return SpendingPeriodTemplate(
            id = UUID.fromString(m["id"] as String),
            userId = UUID.fromString(m["userId"] as String),
            startDate = LocalDate.parse(m["startDate"] as String),
            recurrence = SchedulerRecurrence.fromMap(m["recurrence"] as Map<String, Any>?),
            targetBudgetIds = ((m["targetBudgetIds"] as Collection<*>)).map { UUID.fromString(it.toString()) }.toSet(),
            isActive = m["isActive"] as Boolean,
            endDate = (m["endDate"] as String?)?.let(LocalDate::parse),
        )
    }
}

@Component
class UserCacheCodec : ICacheCodec<User> {
    override fun encode(e: User) = mapOf(
        "id" to e.id.toString(),
        "externalUserId" to e.externalUserId,
        "username" to e.username,
        "email" to e.email.toString(),
        "apiToken" to e.apiToken,
    )

    override fun decode(m: Map<String, Any?>): User {
        return User(
            id = UUID.fromString(m["id"] as String),
            externalUserId = m["externalUserId"] as String,
            username = m["username"] as String,
            email = Email(m["email"] as String),
            apiToken = m["apiToken"] as String,
        )
    }
}

@Component
class ProfileCacheCodec : ICacheCodec<Profile> {
    override fun encode(e: Profile) = mapOf(
        "id" to e.id.toString(),
        "userId" to e.userId.toString(),
        "maxWishlistAmount" to e.maxWishlistAmount,
        "fixSpendPercentage" to e.fixSpendPercentage,
        "varialSpendPercentage" to e.varialSpendPercentage,
        "savingPercentage" to e.savingPercentage,
        "balanceBuffer" to e.balanceBuffer,
    )

    override fun decode(m: Map<String, Any?>): Profile {
        return Profile(
            id = UUID.fromString(m["id"] as String),
            userId = UUID.fromString(m["userId"] as String),
            maxWishlistAmount = (m["maxWishlistAmount"] as Number).toDouble(),
            fixSpendPercentage = (m["fixSpendPercentage"] as Number).toDouble(),
            varialSpendPercentage = (m["varialSpendPercentage"] as Number).toDouble(),
            savingPercentage = (m["savingPercentage"] as Number).toDouble(),
            balanceBuffer = (m["balanceBuffer"] as Number).toDouble(),
        )
    }
}