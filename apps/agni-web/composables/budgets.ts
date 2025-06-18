
export type BudgetType = {
    id: string
    title: string
    target: number
    amount: number
    dateStart: string
    dateEnd: string|null
    period: string|null
    periodTime: number
}

export type PeriodType = {
    id: string
    label: string
}

export const useFetchListPeriod = (): UseApiFetchReturn<PeriodType[]> => {
    const {data, error, refresh} = useAsyncData('period-types', () => fetchListPeriodTypes())

    if (error.value) {
        const toast = useToast()
        const resError = error as Ref<ErrorApi>
        toast.add({title: 'Oops! Erreur', description: resError.value.data.error.message, color: 'error'})
        data.value = []
        return {data: data as Ref<[]>, error: resError, refresh}
    }

    return {data: data as Ref<PeriodType[]>, error: error as Ref<null>, refresh}
}

export const useFetchListBudget = (): UseApiFetchReturn<BudgetType[]> => {
    const { data, error, refresh }  = useAsyncData('budgets', () => fetchListBudgets())  

    if (error.value) {
        const toast = useToast()
        const resError = error as Ref<ErrorApi>
        toast.add({title: 'Oops! Erreur', description: resError.value.data.error.message, color: 'error'})
        data.value = []
        return {data: data as Ref<[]>, error: resError, refresh}
    }

    return {data: data as Ref<BudgetType[]>, error: error as Ref<null>, refresh}
}

export const useFetchBudget = (budget_id: string): UseApiFetchReturn<BudgetType|null> => {
    const { data, error, refresh }  = useAsyncData(`budget-${budget_id}` , () => fetchBudget(budget_id))  

    if (error.value) {
        const toast = useToast()
        const resError = error as Ref<ErrorApi> 
        toast.add({title: 'Oops! Erreur', description: resError.value.data.error.message, color: 'error'})

        return {data: data, error: resError, refresh}
    }

    return {data: data, error: error as Ref<null>, refresh}
}

export async function fetchListPeriodTypes(): Promise<PeriodType[]> {
    try {
        const api = useApiLink()
        const response = await $fetch(`${api}/internal/period-type`)
        const data = (response as {id: string, value: string}[])

        return data.map(value => ({id: value.id, label: value.value}))
    } catch(err) {
        console.log(err)
        return []
    }
}

type BudgetApiType = {
    id: string
    title: string
    currentBalance: number
    period: string|null
    periodTime: number
    target: number
    startDate: string
    updateDate: string
    endDate: string|null
}

export async function fetchListBudgets(): Promise<BudgetType[]> {
    const api = useApiLink()
    const response = await $fetch(`${api}/budgets`)
    const data = (response as {data: BudgetApiType[]}).data

    return data.map(val => ({id: val.id, title: val.title, target: val.target, 
        amount: val.currentBalance, dateEnd: val.endDate ?? null, dateStart: val.startDate, 
        period: val.period ?? null, periodTime: val.periodTime}))
}

export async function fetchBudget(budgetId: string): Promise<BudgetType> {
    const api = useApiLink()
    const response = await $fetch(`${api}/budgets/${budgetId}`)

    const data = (response as {data: BudgetApiType}).data

    return {id: data.id, title: data.title, target: data.target, 
        amount: data.currentBalance, dateEnd: data.endDate ?? null, dateStart: data.startDate, 
        period: data.period ?? null, periodTime: data.periodTime}
}

export type CreateBudgetRequest = {
    title: string,
    target: number,
    dateStart: string,
    period: string,
    periodTime: number,
    dateEnd: string|null
}

export async function fetchCreateBudget(request: CreateBudgetRequest): Promise<void> {
    const toast = useToast()
    try {
        const api = useApiLink()
        const response = await $fetch(`${api}/budgets`, {
            method: 'POST',
            body: {
                title: request.title,
                target: request.target,
                period: request.period,
                periodTime: request.periodTime,
                dateStart: request.dateStart,
                dateEnd: request.dateEnd
            }
        })

        const data = (response as {success: boolean})

        if (!data.success)
            toast.add({ title: 'Oops! Erreur', description: "", color: 'error'})
    
        toast.add({ title: 'Success', description: `Budget ${request.title} cree`, color: 'success' })
    } catch(err) {
        const resData = err as ErrorApi
        toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
    }
}

export type UpdateBudgetRequest = {
    budgetId: string
    title: string,
    target: number,
    dateStart: string,
    period: string,
    periodTime: number,
    dateEnd: string|null
}
export async function fetchUpdateBudget(request: UpdateBudgetRequest): Promise<void> {
    const toast = useToast()
    try {
        const api = useApiLink()
        const response = await $fetch(`${api}/budgets/${request.budgetId}`, {
            method: 'PUT',
            body: {
                title: request.title,
                target: request.target,
                period: request.period,
                periodTime: request.periodTime,
                dateStart: request.dateStart,
                dateEnd: request.dateEnd
            }
        })

        const data = (response as {success: boolean})

        if (!data.success)
            toast.add({ title: 'Oops! Erreur', description: "", color: 'error'})
    
        toast.add({ title: 'Success', description: `Budget ${request.title} mis a jour`, color: 'success' })
    } catch(err) {
        const resData = err as ErrorApi
        toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
    }
}

export async function fetchDeleteBudget(budgetId: string): Promise<void> {
    const toast = useToast();
    try {
        const api = useApiLink()
        const response = await $fetch(`${api}/budgets/${budgetId}`, {
            method: 'DELETE'
        })

        const data = response as {success: boolean}

        if (!data.success) {
            toast.add({ title: 'Oops! Erreur', description: "", color: 'error'})
        }

        toast.add({ title: 'Success', description: `Budget supprime`, color: 'success'})
    } catch(err) {
        const resData = err as ErrorApi
        
        toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
    }
}

export const formatBudgetDataForChart = (budgets: BudgetType[]|null) => {

    let labels: string[] = [] 
    const data: number[]= []
    const reactiveColor: string[] = []
    if (budgets){
        labels = budgets.map(budget => budget.title + " - " + ((budget.amount/budget.target)* 100).toFixed(1) + "%")
        budgets.forEach(budget => {
            data.push(budget.target)
            const alpha = budget.amount/budget.target
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

