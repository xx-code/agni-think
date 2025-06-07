export type GoalRowType = {
    id: string,
    title: string,
    amount: number,
    targetAmount: number
}

type GoalApiType = {
    id: string
    title: string
    description: string
    target: number
    balance: number
}

export const useFetchListGoals = async (): Promise<Ref<GoalRowType[]>> => { 
    const { data, error } = useAsyncData('goals', () => fetchListGoal()) 

    if (error.value) {
        const toast = useToast()
        const resError = error.value.data as ErrorApi
        toast.add({title: 'Oops! Erreur', description: resError.data.error.message, color: 'error'})
        return ref([])
    }

    const budgets = ref(data.value!)

    return budgets 
}

export const useFetchGoal = async (goalId: string): Promise<Ref<GoalRowType | null>> => {
    const { data, error } = useAsyncData(`goal-${goalId}`, () =>  fetchGoal(goalId))

    if (error.value) {
        const toast = useToast()
        const resError = error.value.data as ErrorApi
        toast.add({title: 'Oops! Erreur', description: resError.data.error.message, color: 'error'})
        return data
    }

    return data
}

export async function fetchListGoal(): Promise<GoalRowType[]> {
    const response = await $fetch(`${API_LINK}/save-goals`)

    const data = (await response as {data: GoalApiType[]}).data

    return data.map(val => ({id: val.id, title: val.title, targetAmount: val.target, amount: val.balance}))
}

export async function fetchGoal(goalId: string): Promise<GoalRowType> {
    const response = await $fetch(`${API_LINK}/save-goals/${goalId}`)

    const data = (await response as {data: GoalApiType}).data
    
    return {id: data.id, title: data.title, targetAmount: data.target, amount: data.balance}
}

export type CreateGoalRequest = {
    title: string
    description: string
    target: number
}
export async function fetchCreateGoal(request: CreateGoalRequest): Promise<void> {
    const toast = useToast()
    try {
        const response = await $fetch(`${API_LINK}/save-goals`, {
            method: 'POST',
            body: {
                title: request.title,
                description: request.description,
                target: request.target
            }
        })

        const data = (response as {success: boolean})

        if (!data.success)
            toast.add({ title: 'Oops! Erreur', description: "", color: 'error'})
    
        toast.add({ title: 'Success', description: `Le but d'epargne ${request.title} cree`, color: 'success' })
    } catch(err) {
        const resData = err as ErrorApi
        toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
    }
}

export type UpdateGoalRequest = {
    saveGoalId: string
    title: string
    description: string
    target: number
}
export async function fetchUpdateGoal(request: UpdateGoalRequest): Promise<void> {
    const toast = useToast()
    try {
        const response = await $fetch(`${API_LINK}/save-goals/${request.saveGoalId}`, {
            method: 'PUT',
            body: {
                title: request.title,
                description: request.description,
                target: request.target
            }
        })

        const data = (response as {success: boolean})

        if (!data.success)
            toast.add({ title: 'Oops! Erreur', description: "", color: 'error'})
    
        toast.add({ title: 'Success', description: `Le but d'epargne ${request.title} mis a jour`, color: 'success' })
    } catch(err) {
        const resData = err as ErrorApi
        toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
    } 
}

export async function fetchDeleteSaveGoal(saveGoalId: string, accountDepositId: string): Promise<void> {
    const toast = useToast()
    try {
        const response = await $fetch(`${API_LINK}/save-goals/${saveGoalId}`, {
            method: 'DELETE',
            body: {
                accountDepositId: accountDepositId       
            }
        })

        const data = (response as {success: boolean})

        if (!data.success)
            toast.add({ title: 'Oops! Erreur', description: "", color: 'error'})
    
        toast.add({ title: 'Success', description: `Le but d'epargne supprime`, color: 'success' })
    } catch(err) {
        const resData = err as ErrorApi
        toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
    } 
}

export type IncreaseGoalRequest = {
    saveGoalId: string
    accountFromId: string
    amount: number
}

export async function fetchIncreaseSaveGoal(request: IncreaseGoalRequest): Promise<void> {
    const toast = useToast()
    try {
        const response = await $fetch(`${API_LINK}/save-goals/${request.saveGoalId}/increase-balance`, {
            method: 'PATCH',
            body: {
                accountFromId: request.accountFromId,
                amount: request.amount
            }
        })

        const data = (response as {success: boolean})

        if (!data.success)
            toast.add({ title: 'Oops! Erreur', description: "", color: 'error'})
    
        toast.add({ title: 'Success', description: `Ajout au but d'epargne`, color: 'success' })
    } catch(err) {
        const resData = err as ErrorApi
        toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
    } 
}

export type DescreaseGoalRequest = {
    saveGoalId: string
    accountToId: string
    amount: number
}

export async function fetchDescreaseSaveGoal(request: DescreaseGoalRequest): Promise<void> {
    const toast = useToast()
    try {
        const response = await $fetch(`${API_LINK}/save-goals/${request.saveGoalId}/decrease-balance`, {
            method: 'PATCH',
            body: {
                accountFromId: request.accountToId,
                amount: request.amount
            }
        })

        const data = (response as {success: boolean})

        if (!data.success)
            toast.add({ title: 'Oops! Erreur', description: "", color: 'error'})
    
        toast.add({ title: 'Success', description: `Reduire But d'epargne`, color: 'success' })
    } catch(err) {
        const resData = err as ErrorApi
        toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
    } 
}