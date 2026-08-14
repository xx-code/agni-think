import type { UpdateAccountRequest } from "~/types/api/account";
import type { CreateFundRequest, GetFundResponse } from "~/types/api/fund";
import type { FundForm } from "~/types/form/fund";
import type { Fund, FundCard, FundCardGoal, FundContext, FundGoalState } from "~/types/ui/fund";

export function fundFormToCreateFundRequest(form: FundForm): CreateFundRequest {
    return form
} 

export function fundFormToUpdateFundRequest(form: FundForm): UpdateAccountRequest {
    return form
}

export function fundToFundForm(data: Fund): FundForm {
    return data
}

export function fundResponseToFund(api: GetFundResponse): Fund {
    return api
}

export function fundToFundCard(data: Fund): FundCard {
    const goals = Object.assign([] as Fund['goals'], data.goals)
    goals.sort((a, b) => new Date(b.dueDate).getTime() - new Date(a.dueDate).getTime() )
    return {
        ...data, 
        goalSummary: goals.length > 0 ? {
            numberGoal: goals.length,
            nextDueDate: new Date(goals[0]!.dueDate)
        } : undefined
    }
}

export function fundCardContextToFundGoalCards(data: FundContext): FundCardGoal[] {
    const determineStatus = (dueDate: Date, percentage: number) : FundGoalState => {
        if (getDaysRemaining(dueDate) >= 0 && percentage == 100)
            return 'ACHIEVED'

        if (getDaysRemaining(dueDate) <= 0 && percentage < 100)
            return 'EXPIRED'

        return 'IN_PROGRESS'
    }

    return data.goals.map(i => ({
        id: i.id,
        title: i.title,
        description: i.description,
        targetAmount: i.evaluation.targetAmount,
        currentBalance: i.evaluation.currentBalance,
        percentage: i.evaluation.percentage,
        dueDate: i.dueDate,
        status: determineStatus(i.dueDate, i.evaluation.percentage)
    } satisfies FundCardGoal))
}