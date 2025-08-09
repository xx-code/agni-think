import type { GetAllBudgetResponse } from "~/types/api/budget"

export const formatBudgetDataForChart = (budgets?: GetAllBudgetResponse[]) => {

    let labels: string[] = [] 
    const data: number[]= []
    const reactiveColor: string[] = []
    if (budgets){
        labels = budgets.map(budget => budget.title + " - " + ((budget.currentBalance/budget.target)* 100).toFixed(1) + "%")
        budgets.forEach(budget => {
            data.push(budget.target)
            const alpha = budget.currentBalance/budget.target
            reactiveColor.push(`rgba(102,85,215, ${alpha})`)
        })
    }

    return {
        labels: labels,
        datasets: [{
            label: 'Budgets',
            data: data,
            backgroundColor: reactiveColor,
        }]
    }
}