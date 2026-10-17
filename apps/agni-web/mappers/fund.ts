import type { ListResponse } from "~/types/api";

import type { CreateFundRequest, GetFundResponse, UpdateFundRequest } from "~/types/api/fund";
import type { FundForm } from "~/types/form/fund";
import type { List } from "~/types/ui";
import type { Fund, FundCard, FundCardGoal, FundContext, FundGoalState } from "~/types/ui/fund";

export function toCreateFundRequest(form: FundForm): CreateFundRequest | UpdateFundRequest {
    const { sinkingFundDetail, ...rest } = form as any
    let transformed = { ...rest }
    if (sinkingFundDetail) {
        transformed = {
            ...rest,
            sinkingFundDetail: {
                startDate: sinkingFundDetail.startDate,
                numberOfContributions: Number(sinkingFundDetail.numberOfContributions),
                contributionRecurrence: {
                    period: sinkingFundDetail.contributionRecurrence.period,
                    interval: Number(sinkingFundDetail.contributionRecurrence.interval)
                },
                scheduler: sinkingFundDetail.scheduler ? {
                    date: sinkingFundDetail.scheduler.date,
                    repeater: sinkingFundDetail.scheduler.repeater ? {
                        period: sinkingFundDetail.scheduler.repeater.period,
                        interval: Number(sinkingFundDetail.scheduler.repeater.interval)
                    } : undefined
                } : undefined
            }
        }
    }
    return transformed as CreateFundRequest
} 

export function toUpdateFundRequest(form: FundForm): UpdateFundRequest {
    const { sinkingFundDetail, ...rest } = form as any
    let transformed = { ...rest }
    if (sinkingFundDetail) {
        transformed = {
            ...rest,
            sinkingFundDetail: {
                startDate: sinkingFundDetail.startDate,
                numberOfContributions: Number(sinkingFundDetail.numberOfContributions),
                contributionRecurrence: {
                    period: sinkingFundDetail.contributionRecurrence.period,
                    interval: Number(sinkingFundDetail.contributionRecurrence.interval)
                },
                scheduler: sinkingFundDetail.scheduler ? {
                    date: sinkingFundDetail.scheduler.date,
                    repeater: sinkingFundDetail.scheduler.repeater ? {
                        period: sinkingFundDetail.scheduler.repeater.period,
                        interval: Number(sinkingFundDetail.scheduler.repeater.interval)
                    } : undefined
                } : undefined
            }
        }
    }
    return transformed as UpdateFundRequest
}

export function toFundForm(data: Fund): FundForm {
    return {
        title: data.title,
        description: data.description,
        target: data.target,
        accountId: data.accountId,
        type: data.type,
        sinkingFundDetail: data.sinkingFundDetail ? {
            startDate: data.sinkingFundDetail.startDate instanceof Date ? data.sinkingFundDetail.startDate.toISOString() : data.sinkingFundDetail.startDate,
            numberOfContributions: data.sinkingFundDetail.numberOfContributions,
            contributionRecurrence: {
                period: data.sinkingFundDetail.contributionRecurrence.period,
                interval: data.sinkingFundDetail.contributionRecurrence.interval
            },
            scheduler: data.sinkingFundDetail.fundScheduler ? {
                date: data.sinkingFundDetail.fundScheduler.date instanceof Date ? data.sinkingFundDetail.fundScheduler.date.toISOString() : data.sinkingFundDetail.fundScheduler.date,
                repeater: data.sinkingFundDetail.fundScheduler.repeater ? {
                    period: data.sinkingFundDetail.fundScheduler.repeater.period,
                    interval: data.sinkingFundDetail.fundScheduler.repeater.interval
                } : undefined
            } : undefined
        } : undefined
    }
}

export function toFund(api: GetFundResponse): Fund {
    return {
        id: api.id,
        title: api.title,
        description: api.description,
        target: api.target,
        balance: api.balance,
        type: api.type as any,
        accountId: api.accountId,
        goals: api.goals,
        sinkingFundDetail: api.sinkingFundDetail ? {
            numberOfContributions: api.sinkingFundDetail.numberOfContributions,
            startDate: new Date(api.sinkingFundDetail.startDate),
            contributionRecurrence: {
                period: api.sinkingFundDetail.contributionRecurrence.period,
                interval: api.sinkingFundDetail.contributionRecurrence.interval
            },
            fundScheduler: api.sinkingFundDetail.fundScheduler ? {
                date: new Date(api.sinkingFundDetail.fundScheduler.date),
                repeater: api.sinkingFundDetail.fundScheduler.repeater ? {
                    period: api.sinkingFundDetail.fundScheduler.repeater.period,
                    interval: api.sinkingFundDetail.fundScheduler.repeater.interval
                } : undefined
            } : undefined,
            isClosed: api.sinkingFundDetail.isClosed
        } : undefined
    }
}

export function tolistFund(api: ListResponse<GetFundResponse>): List<Fund> {
    return {
        total: api.total,
        items: api.items.map(toFund)
    }
}

export function toFundCard(data: Fund): FundCard {
    const goals = Object.assign([] as Fund["goals"], data.goals)
    goals.sort((a, b) => new Date(b.dueDate).getTime() - new Date(a.dueDate).getTime() )
    return {
        ...data, 
        goalSummary: goals.length > 0 ? {
            numberGoal: goals.length,
            nextDueDate: new Date(goals[0]!.dueDate)
        } : undefined
    }
}


export function toFundGoalCards(data: FundContext): FundCardGoal[] {
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