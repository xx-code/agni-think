package dev.auguste.agni_api.infras.persistences.jbdc_model

import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Table("categories")
data class JdbcCategoryModel(
    @Id
    @get:JvmName("getIdentifier")
    val categoryId: UUID,

    val title: String,

    val color: String?,

    val iconId: String,

    @Column("is_system")
    val isSystem: Boolean,

    val isArchive: Boolean,

    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
) : JdbcModel() {
    override fun getId(): UUID {
        return categoryId
    }
}

@Component
class JdbcCategoryModelMapper: IMapper<JdbcCategoryModel, Category> {
    override fun toDomain(model: JdbcCategoryModel): Category {
        val category = Category(
            id = model.categoryId,
            title = model.title,
            icon = model.iconId,
            isSystem = model.isSystem,
            color = model.color ?: "",
            isArchived = model.isArchive
        )
        category.initDate(model.createdAt, model.createdAt)
        return category
    }

    override fun toModel(entity: Category): JdbcCategoryModel {
        return JdbcCategoryModel(
            categoryId = entity.id,
            title = entity.title,
            color = entity.color,
            iconId = entity.icon,
            isSystem = entity.isSystem,
            isArchive = entity.isArchived,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "category_id",
        "title" to "title",
        "color" to "color",
        "icon" to "icon_id",
        "isSystem" to "is_system",
        "isArchived" to "is_archive",
        "createdAt" to "created_at",
        "updatedAt" to "updated_at"
    )

    override fun getTableName(): String = "categories"

    override fun getSortField(): Set<String> {
        return setOf("is_system", "name", "created_at", "updated_at")
    }

    override fun getModelClass(): Class<JdbcCategoryModel> = JdbcCategoryModel::class.java
}
