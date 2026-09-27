import { listSpendingPeriodsResponseToListSpendingPeriods, spendingPeriodResponseToSpendingPeriod } from '~/mappers/spending-period'
import { API_ROUTES } from '~/shared/routes'
import type { ListResponse } from '~/types/api'
import type { GetAllSpendingPeriodResponse, GetSpendingPeriodResponse } from '~/types/api/spending-period'
import { isInProgressSpendingPeriodType } from '~/types/constants/spendingPeriod'
import type { SpendingPeriod } from '~/types/ui/spending-period'
import { ApiLinkBuilder } from '~/utils/ApiLinkBuilder'

export type SpendingPeriodAnalyticRange = {
    startDate: string
    endDate?: string
    period: 'Month' | 'Day'
    interval: number
    isSpendingPeriod: boolean
}

export function useInProgressSpendingPeriod() {
    return useAsyncData('overview+spending-period', async () => {
        const list = await ApiLinkBuilder
            .route<ListResponse<GetAllSpendingPeriodResponse>>(API_ROUTES.SPENDING_PERIOD.GET_ALL_SPENDING_PERIOD)
            .query({
                'queryFilter.offset': 0,
                'queryFilter.limit': 0,
                'queryFilter.queryAll': true,
                'queryFilter.sortBy.by': 'start_date',
                'queryFilter.sortBy.ascending': false
            })
            .mapper(listSpendingPeriodsResponseToListSpendingPeriods)
            .execute()

        const inProgress = list.items.find(i => isInProgressSpendingPeriodType(i.state))

        if (!inProgress)
            return undefined

        return await ApiLinkBuilder
            .route<GetSpendingPeriodResponse>(API_ROUTES.SPENDING_PERIOD.GET_SPENDING_PERIOD)
            .params({ id: inProgress.id })
            .mapper(spendingPeriodResponseToSpendingPeriod)
            .execute()
    })
}

// Analytic endpoints only support startDate, so the interval covers the whole spending period
// to get a single bucket from its start date up to now
export function getSpendingPeriodAnalyticRange(period?: SpendingPeriod): SpendingPeriodAnalyticRange {
    if (period) {
        const now = new Date()
        const effectiveEnd = period.endDate.getTime() < now.getTime() ? period.endDate : now
        const days = Math.max(1, Math.ceil((effectiveEnd.getTime() - period.startDate.getTime()) / 86400000))

        return {
            startDate: period.startDate.toISOString(),
            endDate: period.endDate.toISOString(),
            period: 'Day',
            interval: days,
            isSpendingPeriod: true
        }
    }

    const date = new Date()
    date.setDate(1)
    date.setHours(0, 0, 0, 0)

    return {
        startDate: date.toISOString(),
        period: 'Month',
        interval: 1,
        isSpendingPeriod: false
    }
}
