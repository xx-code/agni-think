package dev.auguste.agni_api.infras.persistences.jbdc_model

import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Table("tags")
data class JdbcTagModel(
    @Id
    @get:JvmName("getIdentifier")
    val tagId: UUID,

    val value: String,
    val color: String,
    val isSystem: Boolean,
    val isArchived: Boolean,
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
) : JdbcModel() {
    override fun getId(): UUID {
        return tagId
    }
}

@Component
class JdbcTagModelMapper: IMapper<JdbcTagModel, Tag> {
    override fun toDomain(model: JdbcTagModel): Tag {
        val tag = Tag(
            id = model.tagId,
            value = model.value,
            color = model.color,
            isSystem = model.isSystem,
            isArchived = model.isArchived,
        )
        tag.initDate(model.createdAt, model.updatedAt)

        return tag
    }

    override fun toModel(entity: Tag): JdbcTagModel {
        return JdbcTagModel (
            tagId = entity.id,
            value = entity.value,
            color = entity.color,
            isSystem = entity.isSystem,
            isArchived = entity.isArchived,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "tag_id",
        "value" to "value",
        "color" to "color",
        "isSystem" to "is_system",
        "isArchived" to "is_archived",
        "createdAt" to "created_at",
        "updatedAt" to "updated_at"
    )

    override fun getTableName(): String = "tags"

    override fun getSortField(): Set<String> {
        return setOf("value", "created_at", "updated_at")
    }

    override fun getModelClass(): Class<JdbcTagModel> = JdbcTagModel::class.java
}
