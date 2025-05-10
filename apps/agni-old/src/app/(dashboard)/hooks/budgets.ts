import { api, CallApiError } from "@/lib/api-clients";
import { Budget } from "@/models/budget";
import { useState } from "react";

export function useBudgetsFetching() {
    const [budgets, setBudgets] = useState<Budget[]>([]) 
    const [errorBudget, setErrorBudget] = useState<CallApiError|null>(null)
    const [loading, setLoading] = useState(true)

    const fetchBudgets = async () => {
        setLoading(true)
        try {
            let fetchedBudgets: any[] = await api.get('/budgets')
            let budgets: Budget[] = []

            for (let fetchedBudget of fetchedBudgets) {
                budgets.push({
                    budgetId: fetchedBudget['id'],
                    balance: fetchedBudget['currentBalance'],
                    name: fetchedBudget['title'],
                    endDate: fetchedBudget['endDate'],
                    target: fetchedBudget['target']
                })
            }

            setBudgets(budgets)
        } catch(error: any) {
            let apiError: CallApiError = error
            setErrorBudget(apiError)
        }
        setLoading(false)
    }

    return {budgets, errorBudget, loading, fetchBudgets}
}