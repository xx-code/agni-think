package dev.auguste.agni_api.infras.persistences.jbdc_model

import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.util.UUID

@Table("categories")
data class JdbcCategoryModel(
    @Id
    @get:JvmName("getIdentifier")
    @Column("category_id")
    val id: UUID,

    @Column("title")
    val name: String,

    val color: String?,

    @Column("icon_id")
    val icon: String,

    @Column("is_system")
    val isSystem: Boolean
) : JdbcModel() {
    override fun getId(): UUID {
        return id
    }
}

@Component
class JdbcCategoryModelMapper: IMapper<JdbcCategoryModel, Category> {
    override fun toDomain(model: JdbcCategoryModel): Category {
        return Category(
            id = model.id,
            title = model.name,
            icon = model.icon,
            isSystem = model.isSystem,
            color = model.color ?: ""
        )
    }

    override fun toModel(entity: Category): JdbcCategoryModel {
        return JdbcCategoryModel(
            id = entity.id,
            name = entity.title,
            color = entity.color,
            icon = entity.icon,
            isSystem = entity.isSystem
        )
    }

    override fun getSortField(): Set<String> {
        return setOf("title")
    }
}
