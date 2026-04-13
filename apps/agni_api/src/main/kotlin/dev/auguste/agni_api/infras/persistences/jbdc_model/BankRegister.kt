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
    @Column("bank_register_id")
    val id: UUID,
    @Column("access_code")
    val accessCode: String,
    val title: String,
    val cursor: String,
    @Column("is_active")
    val isActive: Boolean,
    @Column("accounts_linked")
    val accountsLinked: String
) : JdbcModel() {
    override fun getId(): UUID {
        return id
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
            id = model.id,
            accessCode = model.accessCode,
            title = model.title,
            accountsLinked = accountsLinkedJson.map { AccountLinked.fromMap(it) }.toSet(),
            cursor = model.cursor,
            isActive = model.isActive
        )
    }

    override fun toModel(entity: BankRegister): JdbcBankRegisterModel {
        return JdbcBankRegisterModel(
            id = entity.id,
            title = entity.title,
            accessCode = entity.accessCode,
            cursor = entity.cursor,
            isActive = entity.isActive,
            accountsLinked = objectMapper.writeValueAsString(entity.accountslinked.map { objectMapper.writeValueAsString(it.toMap()) })
        )
    }

    override fun getSortField(): Set<String> {
        return setOf()
    }
}