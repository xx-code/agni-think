import type { ListResponse } from "~/types/api";
import type { GetAgentSuggestionResponse } from "~/types/api/agent-suggestion";
import type { AgentSuggestionStatusType, AgentSuggestionType } from "~/types/ui/agent-suggestion";

export function agentSuggestionResponseToAgentSuggestion(data: GetAgentSuggestionResponse): AgentSuggestionType {
    return {
        agentId: data.agentId,
        agentName: data.agentName,
        title: data.title,
        description: data.description,
        confidenceScore: data.confidenceScore,
        status: data.status as AgentSuggestionStatusType
    }
}

export function listAgentSuggestionsResponseToListAgentSuggestions(data: ListResponse<GetAgentSuggestionResponse>): ListResponse<AgentSuggestionType> {
    return {
        items: data.items.map(i => agentSuggestionResponseToAgentSuggestion(i)),
        total: data.total
    }
}
