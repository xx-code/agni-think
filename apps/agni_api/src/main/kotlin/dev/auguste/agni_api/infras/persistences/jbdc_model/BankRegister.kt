package dev.auguste.agni_api.infras.persistences.jbdc_model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.auguste.agni_api.core.entities.BankRegister
import dev.auguste.agni_api.core.value_objects.AccountLinked
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.util.UUID

@Table("bank_registers")
data class JdbcBankRegisterModel(
    @Id
    @get:JvmName("getIdentifier")
    val bankRegisterId: UUID,
    val institutionId: String,
    val accessCode: String,
    val title: String,
    val cursor: String,
    @Column("is_active")
    val isActive: Boolean,
    @Column("accounts_linked")
    val accountsLinked: String
) : JdbcModel() {
    override fun getId(): UUID {
        return bankRegisterId
    }
}

@Component
class JdbcBankRegisterModelMapper(
    private val objectMapper: ObjectMapper
): IMapper<JdbcBankRegisterModel, BankRegister> {
    override fun toDomain(model: JdbcBankRegisterModel): BankRegister {
        val accountsLinkedJson = objectMapper.readValue(model.accountsLinked, Array<String>::class.java).map {
            objectMapper.readValue<Map<String, Any>>(it)
        }.toSet()

        return BankRegister(
            id = model.bankRegisterId,
            institutionId = model.institutionId,
            accessCode = model.accessCode,
            title = model.title,
            accountsLinked = accountsLinkedJson.map { AccountLinked.fromMap(it) }.toSet(),
            cursor = model.cursor,
            isActive = model.isActive
        )
    }

    override fun toModel(entity: BankRegister): JdbcBankRegisterModel {
        return JdbcBankRegisterModel(
            bankRegisterId = entity.id,
            title = entity.title,
            institutionId = entity.institutionId,
            accessCode = entity.accessCode,
            cursor = entity.cursor,
            isActive = entity.isActive,
            accountsLinked = objectMapper.writeValueAsString(entity.accountslinked.map { objectMapper.writeValueAsString(it.toMap()) })
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "bankRegisterId" to "bank_register_id",
        "institutionId" to "institution_id",
        "accessCode" to "access_code",
        "title" to "title",
        "cursor" to "cursor",
        "isActive" to "is_active",
        "accountsLinked" to "accounts_linked"
    )

    override fun getTableName(): String = "bank_registers"

    override fun getSortField(): Set<String> {
        return setOf("isActive", "cursor")
    }

    override fun getModelClass(): Class<JdbcBankRegisterModel> = JdbcBankRegisterModel::class.java
}