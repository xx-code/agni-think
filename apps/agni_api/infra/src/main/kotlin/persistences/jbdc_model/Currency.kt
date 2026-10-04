package persistences.jbdc_model

import domain.entities.Currency
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import persistences.IMapper
import java.util.UUID

@Table("currencies")
data class JdbcCurrencyModel(
    @Id
    @get:JvmName("getIdentifier")
    val currencyId: UUID,
    val name: String,
    val symbol: String,
    val locale: String?,

    @Column("rate_to_base")
    val rateToBase: Double = 1.0,

    @Column("is_base")
    val isBase: Boolean = false
) : JdbcModel() {
    override fun getId(): UUID {
        return currencyId
    }
}

@Component
class JdbcCurrencyModelMapper: IMapper<JdbcCurrencyModel, Currency> {
    override fun toDomain(model: JdbcCurrencyModel): Currency {
        return Currency(
            id = model.id,
            name = model.name,
            symbol = model.symbol,
            locale = model.locale,
            rateToBase = model.rateToBase,
            isBase = model.isBase
        )
    }

    override fun toModel(entity: Currency): JdbcCurrencyModel {
        return JdbcCurrencyModel(
            currencyId = entity.id,
            name = entity.name,
            symbol = entity.symbol,
            locale = entity.locale,
            rateToBase = entity.rateToBase ?: 1.0,
            isBase = entity.isBase
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "currency_id",
        "name" to "name",
        "symbol" to "symbol",
        "locale" to "locale",
        "rateToBase" to "rate_to_base",
        "isBase" to "is_base"
    )

    override fun getTableName(): String = "currencies"

    override fun getSortField(): Set<String> {
        return setOf("rate_to_base")
    }

    override fun getModelClass(): Class<JdbcCurrencyModel> = JdbcCurrencyModel::class.java
}