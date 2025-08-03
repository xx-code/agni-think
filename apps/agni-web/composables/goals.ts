export type GoalType = {
    id: string,
    title: string,
    amount: number,
    desciprtion: string
    targetAmount: number
}

type GoalApiType = {
    id: string
    title: string
    description: string
    target: number
    balance: number
}

export const useFetchListGoals = (): UseApiFetchReturn<GoalType[]> => { 
    const { data, error, refresh } = useAsyncData('goals', () => fetchListGoal()) 

    if (error.value) {
        const toast = useToast()
        const resError = error as Ref<ErrorApi>
        toast.add({title: 'Oops! Erreur', description: resError.value.data.error.message, color: 'error'})

        return {data: ref([]) as Ref<[]>, error: resError, refresh}
    }


    return {data: ref([]) as Ref<GoalType[]>, error: error as Ref<null>, refresh} 
}

export const useFetchGoal = (goalId: string): UseApiFetchReturn<GoalType | null> => {
    const { data, error, refresh } =  useAsyncData(`goal-${goalId}`, () =>  fetchGoal(goalId))

    if (error.value) {
        const toast = useToast()
        const resError = error as Ref<ErrorApi>
        toast.add({title: 'Oops! Erreur', description: resError.value.data.error.message, color: 'error'})
        return {data, error: resError, refresh}
    }

    return {data, error: error as Ref<ErrorApi>, refresh}
}

export async function fetchListGoal(): Promise<GoalType[]> {
    const api = API_LINK() 
    const response = await $fetch(`${api}/save-goals`)

    const data = (await response as {data: GoalApiType[]}).data

    return data.map(val => ({id: val.id, title: val.title, desciprtion: val.description, targetAmount: val.target, amount: val.balance}))
}

export async function fetchGoal(goalId: string): Promise<GoalType> {
    const api = API_LINK() 
    const response = await $fetch(`${api}/save-goals/${goalId}`)

    const data = (await response as {data: GoalApiType}).data
    
    return {id: data.id, title: data.title, targetAmount: data.target, desciprtion: data.description, amount: data.balance}
}

export type CreateGoalRequest = {
    title: string
    description: string
    target: number
}
export async function fetchCreateGoal(request: CreateGoalRequest): Promise<void> {
    const toast = useToast()
    try {
        const api = API_LINK() 
        const response = await $fetch(`${api}/save-goals`, {
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
        console.log(err)
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
        const api = API_LINK() 
        const response = await $fetch(`${api}/save-goals/${request.saveGoalId}`, {
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
        const api = API_LINK() 
        const response = await $fetch(`${api}/save-goals/${saveGoalId}`, {
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
        const api = API_LINK() 
        const response = await $fetch(`${api}/save-goals/${request.saveGoalId}/increase-balance`, {
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
        const api = API_LINK() 
        const response = await $fetch(`${api}/save-goals/${request.saveGoalId}/decrease-balance`, {
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