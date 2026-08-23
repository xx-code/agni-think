import type { AgentAdvisorRequest, AgentPlanningAdvisorResponse, ChatPersonnalFinanceAdvisorRequest, ChatPersonnalFinanceAdvisorResponse } from "~/types/api/agent";
import type { PlanningAgentAdvisorType } from "~/types/ui/agent";
import type { QueryFilterRequest } from "~/types/api";
import { listAgentSuggestionsResponseToListAgentSuggestions } from "~/mappers/agentSuggestion";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";

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

export async function fetchAgentSuggestions(query: QueryFilterRequest) {
    return await ApiLinkBuilder
        .route(API_ROUTES.AGENTS.GET_SUGGESTIONS)
        .query({
            ...query,
            status: 'Pending'
        })
        .mapper(listAgentSuggestionsResponseToListAgentSuggestions)
        .execute()
}

export async function fetchPlanningAdvisorAgent(request: AgentAdvisorRequest): Promise<PlanningAgentAdvisorType> {
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
