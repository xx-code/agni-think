package dev.auguste.agni_api.infras.persistences.jbdc_model

import domain.entities.AgentSuggestion
import domain.enums.AgentSuggestionStatusType
import dev.auguste.agni_api.infras.persistences.IMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import java.util.UUID

@Table("agent_suggestions")
data class JdbcAgentSuggestionModel(
    @Id
    @get:JvmName("getIdentifier")
    val agentSuggestionId: UUID,
    @Column("agent_id")
    val agentId: String,
    @Column("agent_name")
    val agentName: String,
    val title: String,
    val description: String,
    @Column("confidence_score")
    val confidenceScore: Double,
    val status: String
) : JdbcModel() {
    override fun getId(): UUID {
        return agentSuggestionId
    }
}

@Component
class JdbcAgentSuggestionModelMapper: IMapper<JdbcAgentSuggestionModel, AgentSuggestion> {
    override fun toDomain(model: JdbcAgentSuggestionModel): AgentSuggestion {
        return AgentSuggestion(
            id = model.id,
            agentId = model.agentId,
            agentName = model.agentName,
            title = model.title,
            description = model.description,
            confidenceScore = model.confidenceScore,
            status = AgentSuggestionStatusType.fromString(model.status)
        )
    }

    override fun toModel(entity: AgentSuggestion): JdbcAgentSuggestionModel {
        return JdbcAgentSuggestionModel(
            agentSuggestionId = entity.id,
            title = entity.title,
            agentId = entity.agentId,
            agentName = entity.agentName,
            description = entity.description,
            confidenceScore = entity.confidenceScore,
            status = entity.status.toString()
        )
    }

    override fun getEntityModelFieldName(): Map<String, String> = mapOf(
        "id" to "agent_suggestion_id",
        "agentId" to "agent_id",
        "agentName" to "agent_name",
        "title" to "title",
        "description" to "description",
        "confidenceScore" to "confidence_score",
        "status" to "status"
    )

    override fun getTableName(): String = "agent_suggestions"

    override fun getSortField(): Set<String> {
        return setOf("agentId", "agentName", "confidenceScore", "status")
    }

    override fun getModelClass(): Class<JdbcAgentSuggestionModel> = JdbcAgentSuggestionModel::class.java
}