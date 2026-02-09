package dev.auguste.agni_api.infras.persistences.jbdc_model

import dev.auguste.agni_api.core.entities.Currency
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.util.UUID

@Table("currencies")
data class JdbcCurrencyModel(
    @Id
    @Column("currency_id")
    val id: UUID,
    val name: String,
    val symbol: String,
    val locale: String?,

    @Column("rate_to_base")
    val rateToBase: Double = 1.0,

    @Column("is_base")
    val isBase: Boolean = false
)

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
            id = entity.id,
            name = entity.name,
            symbol = entity.symbol,
            locale = entity.locale,
            rateToBase = entity.rateToBase ?: 1.0,
            isBase = entity.isBase
        )
    }

    override fun getSortField(): Set<String> {
        return setOf("rate_to_base")
    }
}