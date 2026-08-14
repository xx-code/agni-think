import { CalendarDate, getLocalTimeZone } from "@internationalized/date";
import type { CreateGoalRequest, GoalResponse, UpdateGoalRequest } from "~/types/api/goal";
import type { GoalForm } from "~/types/form/goal";
import type { FundCardGoal, FundGoalState } from "~/types/ui/fund";
import type { Goal } from "~/types/ui/goal";

export function goalFormToCreateRequest(form: GoalForm): CreateGoalRequest {
    return {...form, status: 0, targetDate: form.targetDate.toISOString()}
}

export function goalFormToUpdateRequest(form: GoalForm): UpdateGoalRequest {
    return {...form, status: 0, targetDate: form.targetDate.toISOString()}
}

export function goalResponseToGoal(api: GoalResponse): Goal {
    return {...api, dueDate: new Date(api.dueDate )}
}

export function goalToGoalForm(data: Goal): GoalForm {
    const date = new Date(data.dueDate)
    return {
        title: data.title,
        description: data.description,
        status: data.status,
        targetAmount: data.targetAmount,
        targetDate: date,
        targetSourceId: data.targetSourceId,
        type: data.type
    }
}

export function goalToFundGoalCards(data: Goal): FundCardGoal {
    const determineStatus = (dueDate: Date, percentage: number) : FundGoalState => {
        if (getDaysRemaining(dueDate) >= 0 && percentage == 100)
            return 'ACHIEVED'

        if (getDaysRemaining(dueDate) <= 0 && percentage < 100)
            return 'EXPIRED'

        return 'IN_PROGRESS'
    }

    return {
        id: data.id,
        title: data.title,
        description: data.description,
        targetAmount: data.targetAmount,
        currentBalance: data.evaluation.currentBalance,
        percentage: data.evaluation.progressPercentage,
        dueDate: data.dueDate,
        status: determineStatus(data.dueDate, data.evaluation.progressPercentage)
    }
}