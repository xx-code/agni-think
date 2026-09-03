package dev.auguste.agni_api.infras.persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Color
import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.interfaces.IAccountDetail
import dev.auguste.agni_api.core.value_objects.BrokingAccountDetail
import dev.auguste.agni_api.core.value_objects.BusinessAccountDetail
import dev.auguste.agni_api.core.value_objects.CheckingAccountDetail
import dev.auguste.agni_api.core.value_objects.CreditCardAccountDetail
import dev.auguste.agni_api.core.value_objects.SavingAccountDetail
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.util.UUID

@Table("accounts")
data class JbdcAccountModel(
    @Id
    @get:JvmName("getIdentifier")
    @Column("account_id")
    val id: UUID,
    @Column("title")
    val name: String,
    val balance: Double,
    val type: String,
    @Column("currency_id")
    val currencyId: UUID?,
    val detail: String?,
    val color: String
) : JdbcModel() {
    override fun getId(): UUID {
        return id
    }
}

@Component
class JdbcAccountModelMapper(
    private val objectMapper: ObjectMapper
): IMapper<JbdcAccountModel, Account> {
    override fun toDomain(model: JbdcAccountModel): Account {
        var detail: IAccountDetail = CheckingAccountDetail(0.0)

        if (model.detail != null) {
            val detailMap = model.detail.let {
                objectMapper.readValue<Map<String, Any>?>(it)
            } ?: emptyMap()


            detail = when(AccountType.fromString(model.type)) {
                AccountType.SAVING -> SavingAccountDetail.fromMap(detailMap)
                AccountType.CREDIT_CARD-> CreditCardAccountDetail.fromMap(detailMap)
                AccountType.CHECKING -> CheckingAccountDetail.fromMap(detailMap)
                AccountType.BROKING-> BrokingAccountDetail.fromMap(detailMap)
                AccountType.BUSINESS -> BusinessAccountDetail(0.0)
            }
        }

        return Account(
            id = model.id,
            title = model.name,
            balance = model.balance,
            currencyId = model.currencyId,
            detail = detail,
            color = Color(model.color)
        )
    }

    override fun toModel(entity: Account): JbdcAccountModel {
        val detailJson = jacksonObjectMapper().writeValueAsString(entity.detail.toMap())

        return JbdcAccountModel(
            id = entity.id,
            name = entity.title,
            type = entity.detail.getType().value,
            balance = entity.balance,
            currencyId = entity.currencyId,
            detail = detailJson,
            color = entity.color.toString()
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> {
        TODO("Not yet implemented")
    }

    override fun getTableName(): String {
        TODO("Not yet implemented")
    }

    override fun getSortField(): Set<String> {
        return setOf("title")
    }

    override fun getModelClass(): Class<JbdcAccountModel> {
        TODO("Not yet implemented")
    }
}