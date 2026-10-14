package persistences.jbdc_model

import domain.entities.Email
import domain.entities.User
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import persistences.IMapper
import java.time.LocalDateTime
import java.util.UUID

@Table("users")
data class JdbcUserModel(
    @Id
    @get:JvmName("getIdentifier")
    val userId: UUID,

    val externalId: String,
    val username: String,
    val email: String,
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
) : JdbcModel() {
    override fun getId(): UUID {
        return userId
    }
}

@Component
class JdbcUserModelMapper: IMapper<JdbcUserModel, User> {
    override fun toDomain(model: JdbcUserModel): User {
        val user = User(
            id = model.userId,
            externalUserId = model.externalId,
            username = model.username,
            email = Email(model.email)
        )

        user.initDate(model.createdAt, model.updatedAt)

        return user
    }

    override fun toModel(entity: User): JdbcUserModel {
        return JdbcUserModel(
            userId = entity.id,
            externalId = entity.externalUserId,
            username = entity.username,
            email = entity.email.toString(),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "user_id",
        "userId" to "user_id",
        "externalUserId" to "external_id",
        "username" to "username",
        "email" to "email",
        "createdAt" to "created_at",
        "updatedAt" to "updated_at",
    )

    override fun getTableName(): String = "users"

    override fun getSortField(): Set<String> = setOf()

    override fun getModelClass(): Class<JdbcUserModel> = JdbcUserModel::class.java
}
