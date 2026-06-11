import type { AgentAdvisorRequest, AgentPlanningAdvisorResponse, ChatPersonnalFinanceAdvisorRequest, ChatPersonnalFinanceAdvisorResponse } from "~/types/api/agent";
import type { GetAgentSuggestionResponse } from "~/types/api/agent-suggestion";
import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { AgentSuggestionStatusType, AgentSuggestionType } from "~/types/ui/agent-suggestion";
import type { PlanningAgentAdvisorType } from "~/types/ui/agent";

export async function askToFinancePersonnalAdvisor(request: ChatPersonnalFinanceAdvisorRequest): Promise<ChatPersonnalFinanceAdvisorResponse> {
    const res = await $fetch<string>(`${getApiAgent()}/chat`, {
        method: 'POST',
        body: request
    });

    return {
        message: res
    };
}

export async function fetchLlmModels(): Promise<string[]> {
    return await $fetch(`${getApiAgent()}/models`, {
        method: 'GET'
    });
}

export async function useTreatInvoiceText(text: string): Promise<string> {
    return await $fetch(`${getApiAgent()}/treat-unformat-transaction`, {
        method: 'POST',
        body: { text: text }
    });
}

export async function fetchAgentSuggestions(query: QueryFilterRequest): Promise<ListResponse<AgentSuggestionType>> {
    const res = await $fetch<ListResponse<GetAgentSuggestionResponse>>(`api/agent-suggestions`, {
        method: 'GET',
        query: {
            ...query,
            status: 'Pending'
        }
    });

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
    };
}

export async function fetchPlanningAdvisorAgent(request: AgentAdvisorRequest): Promise<PlanningAgentAdvisorType> {
    console.log(request);
    const response = await $fetch<AgentPlanningAdvisorResponse>(`${getApiAgent()}/analytics/save-goal-planning`, {
        method: 'POST',
        body: request
    });

    return {
        comment: response.comment,
        suggestGoalPlanning: response.suggestGoalPlanning.map(i => ({
            amountSuggest: i.amountSuggest,
            reason: i.reason,
            saveGoalId: i.saveGoalId,
            title: i.title
        }))
    };
}
