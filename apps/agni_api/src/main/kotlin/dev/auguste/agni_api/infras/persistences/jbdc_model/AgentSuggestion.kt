package dev.auguste.agni_api.infras.persistences.jbdc_model

import dev.auguste.agni_api.core.entities.AgentSuggestion
import dev.auguste.agni_api.core.entities.enums.AgentSuggestionStatusType
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
    @Column("agent_suggestion_id")
    val id: UUID,
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
        return id
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
            id = entity.id,
            title = entity.title,
            agentId = entity.agentId,
            agentName = entity.agentName,
            description = entity.description,
            confidenceScore = entity.confidenceScore,
            status = entity.status.toString()
        )
    }

    override fun getSortField(): Set<String> {
        return setOf("agentId", "agentName", "confidenceScore", "status")
    }
}