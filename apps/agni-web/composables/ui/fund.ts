import type { FundContext } from "~/types/ui/fund"
import { fetchFund } from "../api/funds"
import { fetchAllGoal } from "../api/goals"

export function useFundCardContext() {
    const isOpen = useState('fundCardContext:isOpen', () => false)
    const isLoading = useState('fundCardContext:isLoading', () => false)
    const error = useState<string|null>('fundCardContext:error', () => null)
    const fund = useState<FundContext|null>('fundCardContext:fund', () => null)

    const openFund = async (fundId: string) => {
        isLoading.value = true
        try {
            const [resFund, resGoals] = await Promise.all([
                fetchFund(fundId),
                fetchAllGoal({ limit: 0, offset: 4, queryAll: true, sourceId: fundId })
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
