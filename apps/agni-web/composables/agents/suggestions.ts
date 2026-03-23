import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetAgentSuggestionResponse } from "~/types/api/agent-suggestion";
import type { AgentSuggestionStatusType, AgentSuggestionType } from "~/types/ui/agent-suggestion";

export async function fetchAgentSuggestions(query: QueryFilterRequest) : Promise<ListResponse<AgentSuggestionType>>{
    const res = await $fetch<ListResponse<GetAgentSuggestionResponse>>("/api/agents/suggestions", {
        method: 'GET',
        query: {
            ...query,
            status: 'Pending'
        }
    })

    return {
        items: res.items.map(i => ({
            agentId: i.agentId,
            agentName: i.agentName,
            confidenceScore: i.confidenceScore,
            title: i.title,
            description: i.description,
            status: i.status as AgentSuggestionStatusType
        } satisfies AgentSuggestionType)),
        total: res.total
    } 
}