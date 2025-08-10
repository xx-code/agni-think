import IAgentPlanningAdvisor, { AgentPlanningAdvisorInput, AgentPlanningAdvisorOutput } from "@core/agents/agentPlanningAdvisor";
import axios from "axios";
import { json } from "stream/consumers";

type RequestHttpAgent = {
    current_amount_in_investissment: number,
    current_amount_in_saving: number,
    percent_of_net_income_saving_and_investissment: number,
    net_income: number,
    amount_to_allocate: number,
    goals:{
        uuid: string,
        description: string,
        target: number,
        score: number,
        current_balance: number,
        desir_value: number,
        importance: number,
        wish_due_date?: string
    }[],
    wish_spends:{
        amount: number,
        description: string
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
                    current_amount_in_investissment: input.currentAmountInInvestissment,
                    current_amount_in_saving: input.currentAmountInInvestissment,
                    goals: input.goals.map(i => ({
                        uuid: i.id,
                        current_balance: i.currentBalance,
                        description: i.description,
                        desir_value: i.desirValue,
                        importance: i.importance,
                        score: i.score,
                        target: i.target,
                        wish_due_date: i.wishDueDate
                    })),
                    net_income: input.income,
                    percent_of_net_income_saving_and_investissment: input.percentForSavingAndInvestissment,
                    wish_spends: input.wishSpends?.map(i => ({
                        amount: i.amount,
                        description: i.description
                    })) || []
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
