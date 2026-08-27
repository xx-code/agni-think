import type { FundContext } from "~/types/ui/fund"
import type { GetFundResponse, QueryFilterFundRequest } from "~/types/api/fund"
import type { ListResponse } from "~/types/api"
import type { GoalResponse, GoalQueryFilterRequest } from "~/types/api/goal"
import { fundResponseToFund } from "~/mappers/fund"
import { goalResponseToGoal } from "~/mappers/goal"
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder"
import { API_ROUTES } from "~/shared/routes"

export function useFundCardContext() {
    const isOpen = useState('fundCardContext:isOpen', () => false)
    const isLoading = useState('fundCardContext:isLoading', () => false)
    const error = useState<string|null>('fundCardContext:error', () => null)
    const fund = useState<FundContext|null>('fundCardContext:fund', () => null)

    const openFund = async (fundId: string) => {
        isLoading.value = true
        try {
            const [resFund, resGoals] = await Promise.all([
                ApiLinkBuilder
                    .route<GetFundResponse>(API_ROUTES.FUNDS.GET_FUND)
                    .params({ id: fundId })
                    .mapper(fundResponseToFund)
                    .execute(),
                ApiLinkBuilder
                    .route<ListResponse<GoalResponse>>(API_ROUTES.GOALS.GET_GOALS)
                    .query({ limit: 0, offset: 4, queryAll: true, sourceId: fundId } as GoalQueryFilterRequest)
                    .execute()
                    .then(res => ({
                        items: res.items.map(i => goalResponseToGoal(i)),
                        total: res.total
                    }))
            ])

            fund.value = {
                ...resFund,
                goals: resGoals.items.map(i => ({
                    ...i,
                    dueDate: new Date(i.dueDate),
                    evaluation: {
                        targetAmount: i.targetAmount, 
                        currentBalance: i.evaluation.currentBalance,
                        percentage: i.evaluation.progressPercentage,
                    }
                }))
            }

            console.log(fund.value)

            isOpen.value = true
            error.value = null
        } catch(err: any) {
            console.log(err)
            error.value = err.message
        } finally {
            isLoading.value = false
        }
    }

    const close = () => {
        fund.value = null
        isOpen.value = false
    }

    return {
        isOpen,
        isLoading,
        error,
        fund,
        openFund,
        close
    }
}
