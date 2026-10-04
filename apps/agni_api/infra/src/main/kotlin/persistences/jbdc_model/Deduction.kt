package persistences.jbdc_model

import domain.entities.Deduction
import domain.enums.DeductionBaseType
import domain.enums.DeductionModeType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import persistences.IMapper
import java.util.UUID

@Table("deductions")
data class JdbcDeductionModel(
    @Id
    @get:JvmName("getIdentifier")
    val deductionId: UUID,

    val title: String,

    val description: String,
    val base: String,
    val mode: String
) : JdbcModel() {
    override fun getId(): UUID {
        return deductionId
    }
}

@Component
class JdbcDeductionModelMapper: IMapper<JdbcDeductionModel, Deduction> {
    override fun toDomain(model: JdbcDeductionModel): Deduction {
        return Deduction(
            id = model.id,
            title = model.title,
            base = DeductionBaseType.fromString(model.base),
            mode = DeductionModeType.fromString(model.mode),
            description = model.description
        )
    }

    override fun toModel(entity: Deduction): JdbcDeductionModel {
        return JdbcDeductionModel(
            deductionId = entity.id,
            title = entity.title,
            description = entity.description,
            base = entity.base.value,
            mode = entity.mode.value
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "deduction_id",
        "title" to "title",
        "description" to "description",
        "base" to "base",
        "mode" to "mode"
    )

    override fun getTableName(): String = "deductions"

    override fun getSortField(): Set<String> {
        return setOf("title")
    }

    override fun getModelClass(): Class<JdbcDeductionModel> = JdbcDeductionModel::class.java
}