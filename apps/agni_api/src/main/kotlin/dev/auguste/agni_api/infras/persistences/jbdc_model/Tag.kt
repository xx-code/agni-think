package dev.auguste.agni_api.infras.persistences.jbdc_model

import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.util.UUID

@Table("tags")
data class JdbcTagModel(
    @Id
    @get:JvmName("getIdentifier")
    @Column("tag_id")
    val id: UUID,

    val value: String,
    val color: String,

    @Column("is_system")
    val isSystem: Boolean
) : JdbcModel() {
    override fun getId(): UUID {
        return id
    }
}

@Component
class JdbcTagModelMapper: IMapper<JdbcTagModel, Tag> {
    override fun toDomain(model: JdbcTagModel): Tag {
        return Tag(
            id = model.id,
            value = model.value,
            color = model.color,
            isSystem = model.isSystem
        )
    }

    override fun toModel(entity: Tag): JdbcTagModel {
        return JdbcTagModel (
            id = entity.id,
            value = entity.value,
            color = entity.color,
            isSystem = entity.isSystem
        )
    }

    override fun getSortField(): Set<String> {
        return setOf("title")
    }
}
