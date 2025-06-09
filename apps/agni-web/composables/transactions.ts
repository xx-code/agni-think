type TransactionApiType = {
    accountId: string 
    transactionId: string
    amount: number
    date: string
    description: string
    recordType: string
    category: {
        id: string
        title: string
        icon: string
        color: string
    },
    type: string,
    tags: {id: string, value: string, color: string}[],
    budgets: string[]
}

export type TransactionType = {
    id: string
    accountId: string
    description: string
    date: string
    amount: number
    type: string,
    recordType: string,
    category: {
        id: string
        title: string
        icon: string
        color: string
    }
    budgets: {id: string, title: string}[]
    tags: {id: string, value: string, color: string}[]
}

export type TransactionPagination = {
    transactions: TransactionType[],
    maxPage: number
}

export type TransactionTypeType = {
    id: string
    label: string
}

export const useFetchListTransactionType = async (): Promise<Ref<TransactionTypeType[]>> => {
    const {data, error} = await useAsyncData('transaction-types', () => fetchListTransactionType())

    if (error.value) {
        const toast = useToast()
        const resError = error.value.data as ErrorApi
        toast.add({title: 'Oops! Erreur', description: resError.data.error.message, color: 'error'})
        return ref([])
    }

    const items = ref(data.value!)

    return items
}


export const useFetchListTransactions = async (filter?:FilterTransactions): Promise<Ref<TransactionPagination>> => {
    const { data, error } = await useAsyncData('transactions', () => fetchListTransaction(filter)) 

    if (error.value) {
        console.log(error)
        const toast = useToast()
        const resError = error.value.data as ErrorApi
        toast.add({title: 'Oops! Erreur', description: resError.data.error.message, color: 'error'})
        return ref({transactions: [], maxPage: 0})
    }

    const items = ref(data.value!)

    return items
} 

export const useFetchTransaction = async (transactionId: string): Promise<Ref<TransactionType|null>> => {
    const { data, error } = await useAsyncData(`transactions-${transactionId}`, () => fetchTransaction(transactionId)) 

    if (error.value) {
        const toast = useToast()
        const resError = error.value.data as ErrorApi
        toast.add({title: 'Oops! Erreur', description: resError.data.error.message, color: 'error'})
        return data
    }

    return data
}

export async function fetchListTransactionType(): Promise<TransactionTypeType[]> {
    const response = await $fetch(`${API_LINK}/internal/transaction-type`)
    const data = (response as {id: string, value: string}[])

    return data.map(value => ({id: value.id, label: value.value}))
}


export type FilterBalanceTransactions = {
    accountIds?: string[],
    tagIds?: string[],
    categoryIds?: string[],
    budgetIds?: string[],
    dateStart?: string,
    dateEnd?: string,
    types?: string,
    minPrice?: number,
    maxPrice?: number 
}

export type FilterTransactions = {
    page?: number,
    limit?: number,
    sortBy?: string,
    sortSense?: string,
    accountFilter?: string[],
    categoryFilter?: string[],
    budgetFilter?: string[],
    tagFilter?: string[],
    dateStart?: string,
    dateEnd?: string,
    types?: string[],
    minPrice?: string,
    maxPrice?: string
}

export async function fetchListTransaction(filter?: FilterTransactions): Promise<TransactionPagination> {
    const response = await $fetch(`${API_LINK}/transactions`, {
        query: filter
    })
    const data = (response as {data: {transactions: TransactionApiType[], maxPages: number} }).data

    return {
        transactions: data.transactions.map(
            data => ({
                id: data.transactionId, 
                accountId: data.accountId,
                category: {id: data.category.id, title: data.category.title, icon: data.category.icon, color: data.category.color},
                description: data.description,
                date: data.date,
                amount: data.amount,
                type: data.type,
                recordType: data.recordType,
                tags: data.tags.map(tag => ({id: tag.id, value: tag.value, color: tag.color})),
                budgets: data.budgets.map(budg => ({id: budg, title: ''}))
            })
        ),
        maxPage: data.maxPages 
    }
}

export async function fetchTransaction(transactionId: string): Promise<TransactionType> {
    const response = await $fetch(`${API_LINK}/transactions/${transactionId}`)
    const data = (response as {data: TransactionApiType }).data

    return {
        id: data.transactionId, 
        accountId: data.accountId,
        category: {id: data.category.id, title: data.category.title, icon: data.category.icon, color: data.category.color},
        description: data.description,
        date: data.date,
        amount: data.amount,
        recordType: data.recordType,
        type: data.type,
        tags: data.tags.map(tag => ({id: tag.id, value: tag.value, color: tag.color})),
        budgets: data.budgets.map(budg => ({id: budg, title: ''}))
    }
}

export async function fetchBalance(filter?: FilterBalanceTransactions): Promise<number> {
    const response = await $fetch(`${API_LINK}/transactions-balance`, {
        query: filter
    })
    const data = (response as {data: {balance: number}}).data

    return data.balance
}

export type CreateTransactionRequest = {
    accountId: string,
    categoryId: string,
    amount: number,
    description: string,
    tagIds: string[],
    budgetIds: string[],
    type: string,
    date: string
}

export async function fetchCreateTransaction(request: CreateTransactionRequest): Promise<void> {
    const toast = useToast()
    try {
        const response = await $fetch(`${API_LINK}/transactions`, {
            method: 'POST',
            body: {
                accountId: request.accountId,
                accountRef: request.accountId,
                categoryId: request.categoryId,
                amount: request.amount,
                description: request.description,
                tagIds: request.tagIds,
                budgetIds: request.budgetIds,
                type: request.type,
                date: request.date
            }
        })

        const data = (response as {success: boolean})

        if (!data.success)
            toast.add({ title: 'Oops! Erreur', description: "", color: 'error'})
    
        toast.add({ title: 'Success', description: `Nouvelle transaction ajoute`, color: 'success' })
    } catch(err) {
        const resData = err as ErrorApi
        toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
    }
}

export type UpdateTransactionRequest = {
    transactionId: string,
    accountId: string,
    categoryId: string,
    amount: number,
    description: string,
    tagIds: string[],
    budgetIds: string[],
    type: string,
    date: string
}


export async function fetchUpdateTransaction(request: UpdateTransactionRequest): Promise<void> {
    const toast = useToast()
    try {
        const response = await $fetch(`${API_LINK}/transactions/${request.transactionId}`, {
            method: 'PUT',
            body: {
                accountId: request.accountId,
                accountRef: request.accountId,
                categoryId: request.categoryId,
                amount: request.amount,
                description: request.description,
                tagIds: request.tagIds,
                budgetIds: request.budgetIds,
                type: request.type,
                date: request.date
            }
        })

        const data = (response as {success: boolean})

        if (!data.success)
            toast.add({ title: 'Oops! Erreur', description: "", color: 'error'})
    
        toast.add({ title: 'Success', description: `Transaction mis a jour`, color: 'success' })
    } catch(err) {
        const resData = err as ErrorApi
        toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
    }
}

export async function fetchDeleteTransaction(transactionId: string): Promise<void> {
    const toast = useToast();
    try {
        const response = await $fetch(`${API_LINK}/transactions/${transactionId}`, {
            method: 'DELETE'
        })

        const data = response as {success: boolean}

        if (!data.success) {
            toast.add({ title: 'Oops! Erreur', description: "", color: 'error'})
        }

        toast.add({ title: 'Success', description: `transaction supprime`, color: 'success'})
    } catch(err) {
        const resData = err as ErrorApi
        
        toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
    }
}

export type TransfertRequest = {
    accountFromId: string
    accountToId: string
    amount: number
    date: string
}
export async function fetchTransfertBetweenAccount(request: TransfertRequest) {
    const toast = useToast();
    try {
        const response = await $fetch(`${API_LINK}/transfert-transaction`, {
            method: 'POST',
            body: {
                accountFromId: request.accountFromId,
                accountToId: request.accountToId,
                date: request.date,
                amount: request.amount
            }
        })

        const data = response as {success: boolean}

        if (!data.success) {
            toast.add({ title: 'Oops! Erreur', description: "", color: 'error'})
        }

        toast.add({ title: 'Success', description: `transfert de transaction`, color: 'success'})
    } catch(err) {
        const resData = err as ErrorApi
        
        toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
    }
}

export type FreezeTransactionRequest = {
    accountId: string
    amount: number
    endDate: string
}
export async function fetchFreezeTransaction(request: FreezeTransactionRequest) {
    const toast = useToast();
    try {
        const response = await $fetch(`${API_LINK}/freeze-transaction`, {
            method: 'POST',
            body: {
                accountId: request.accountId,
                amount: request.amount,
                endDate: request.endDate
            }
        })

        const data = response as {success: boolean}

        if (!data.success) {
            toast.add({ title: 'Oops! Erreur', description: "", color: 'error'})
        }

        toast.add({ title: 'Success', description: `transaction freezer`, color: 'success'})
    } catch(err) {
        const resData = err as ErrorApi
        
        toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
    }
}