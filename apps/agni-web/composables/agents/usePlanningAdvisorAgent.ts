import type { AgentAdvisorRequest, AgentPlanningAdvisorResponse } from "~/types/api/agent";
import type { AgentSavegoalAdviceType, PlanningAgentAdvisorType } from "~/types/ui/agent";
import type { UseApiFetchReturn } from "~/types/utils";

export default function usePlanningAdvisorAgent(request: AgentAdvisorRequest): UseApiFetchReturn<PlanningAgentAdvisorType>{
    const { data, error, refresh, status } = useFetch('/api/agents/planningAdvisor', {
        method: 'POST',
        query: request,
        transform: (data: AgentPlanningAdvisorResponse) => {
            return {
                comment: data.comment,
                suggestGoalPlanning: data.suggestGoalPlanning.map(i => ({
                    amountSuggest: i.amountSuggest,
                    reason: i.reason,
                    saveGoalId: i.saveGoalId,
                    title: i.title
                }))
            } satisfies PlanningAgentAdvisorType
        }
    });

    return { data, error, refresh, status }
}

export async function fetchPlanningAdvisorAgent(request: AgentAdvisorRequest): Promise<PlanningAgentAdvisorType>{
    const response = await $fetch<AgentPlanningAdvisorResponse>('/api/agents/planningAdvisor', {
        method: 'POST',
        query: request 
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