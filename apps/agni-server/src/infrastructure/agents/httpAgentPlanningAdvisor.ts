import IAgentPlanningAdvisor, { AgentPlanningAdvisorInput, AgentPlanningAdvisorOutput } from "@core/agents/agentPlanningAdvisor"; import axios from "axios";

type RequestHttpAgent = {
    current_amount_in_investissment: number,
    current_amount_in_saving: number,
    percent_of_net_income_saving_and_investissment: number,
    net_income: number,
    amount_to_allocate: number,
    future_amount_to_allocate: number
    goals:{
        uuid: string,
        description: string,
        target: number,
        score: number,
        amount_in_goal: number,
        left_amount: number
        desir_value: number,
        importance: number,
        wish_due_date?: string
    }[]
}

type ResponseHttpAgent = {
    goals: {
        id: string,
        score: number,
        suggest_amount: number,
        reasoning: string
    }[],
    comment: string
}

export default class HttpAgentPlanningAdvisor implements IAgentPlanningAdvisor {
    async process(input: AgentPlanningAdvisorInput): Promise<AgentPlanningAdvisorOutput> {
        try {
            const api = process.env.API_AGENT_URL || 'http://127.0.0.1:8000'
            const res = await axios.post(api + "/agents/" + "planning-advisor",{
                    amount_to_allocate: input.amountToAllocate,
                    future_amount_to_allocate: input.futureAmountToAllocate,
                    current_amount_in_investissment: input.currentAmountInInvestissment,
                    current_amount_in_saving: input.currentAmountInSaving,
                    goals: input.goals.map(i => ({
                        uuid: i.id,
                        description: i.description,
                        desir_value: i.desirValue,
                        amount_in_goal: i.currentBalance,
                        left_amount: i.target - i.currentBalance,
                        importance: i.importance,
                        score: i.score,
                        target: i.target,
                        wish_due_date: i.wishDueDate
                    })),
                    net_income: input.income,
                    percent_of_net_income_saving_and_investissment: input.percentForSavingAndInvestissment,
                } satisfies RequestHttpAgent
            );

            const data: ResponseHttpAgent = res.data

            return {
                comment: data.comment,
                goals: data.goals.map(i => ({
                    id: i.id,
                    reasoning: i.reasoning,
                    score: i.score,
                    suggestAmount: i.suggest_amount
                }))
            } 

        } catch(err: any) {
            const error = err.response
            if (error)
                console.log(error.data)
            return {comment: 'Agent indisponible', goals: []};
        } 
    }
} 
