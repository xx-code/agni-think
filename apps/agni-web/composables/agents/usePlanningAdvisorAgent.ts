import type { AgentAdvisorRequest, AgentPlanningAdvisorResponse } from "~/types/api/agent";
import type { PlanningAgentAdvisorType } from "~/types/ui/agent";

export async function fetchPlanningAdvisorAgent(request: AgentAdvisorRequest): Promise<PlanningAgentAdvisorType>{
    console.log(request)
    const response = await $fetch<AgentPlanningAdvisorResponse>('/api/agents/planningAdvisor', {
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