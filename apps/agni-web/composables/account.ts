export const ALL_ACCOUNT_ID = 'all';

export type AccountType = {
    id: string
    title: string
    balance: number
    type: string
}

export type ResumeAccountType = AccountType & {
    pastBalanceDetail: {
        balance: number,
        diffPercent: number,
        doIncrease: boolean
    }
}


function sumTotalBalance(accounts: ResumeAccountType[]): [number, number] {
    let total = 0 
    let pastTotal = 0
    accounts.forEach(acc => {
        total += acc.balance 
        pastTotal += acc.pastBalanceDetail.balance
    }) 

    return [total, pastTotal]
}

export const useFetchResumeAccount = async (): Promise<{data: Ref<ResumeAccountType[]>, refresh: () => Promise<void>}> => {

    const {data, refresh} = await useFetchListAccount();
    const resumeAccounts: ResumeAccountType[] = []
    for(const account of data.value) {
        // fetch passs data
        resumeAccounts.push({...account, pastBalanceDetail: {balance: 0, diffPercent: 0, doIncrease: true}})
    } 

    const totals = sumTotalBalance(resumeAccounts)
    const totalAccount: ResumeAccountType= {
        id: ALL_ACCOUNT_ID,
        title: 'Total Balance',
        balance: totals[0],
        pastBalanceDetail: {
            balance: totals[1],
            diffPercent: Number((Math.abs(((10751/10000) * 100) - 100)).toFixed(2)), 
            doIncrease: totals[1] < totals[0]
        },
        type: ''
    }

    resumeAccounts.unshift(totalAccount)

    return {data: ref(resumeAccounts), refresh: refresh}
}


export const useFetchListAccount = async (): Promise<{data: Ref<AccountType[]>, refresh: () => Promise<void>}> => {
    const {data, error, refresh} = await useFetch('http://localhost:5001/v1/accounts')
    if (error.value) {
        const toast = useToast()
        toast.add({title: 'Oops! Erreur type de compte', description: error.value.data, color: 'error'})
        return {data: ref([]), refresh: refresh }
    }

    const resData = (data.value as {data: {accountId: string, title: string, balance: number, type: string}[]}) 

    let results = ref<AccountType[]>(resData.data.map(val => ({id: val.accountId, title: val.title, balance: val.balance, type: val.type})))

    return {data: ref(results), refresh: refresh}
}

export type AccountTypeType = {
    id: string,
    label: string
}

export const useFetchListAccountTypes = async (): Promise<Ref<AccountTypeType[]>> => { 
    const { data, error } = await useFetch('http://localhost:5001/v1/internal/account-type')
    if (error.value) {
        const toast = useToast()
        toast.add({title: 'Oops! Erreur type de compte', description: error.value.data, color: 'error'})
        return ref([])
    }
    let results = ref<AccountTypeType[]>((data.value as { id: string, value: string }[]).map(val => ({id: val.id, label: val.value})))
    
    return results
}

export const useFetchAccount = async (accountId: string): Promise<Ref<AccountType|null>> => {
    const {data, error} = await useFetch(`http://localhost:5001/v1/accounts/${accountId}`)
    if (error.value) {
        const toast = useToast()
        toast.add({title: 'Oops! Erreur type de compte', description: error.value.data, color: 'error'})
        return ref(null)
    }

    let resData = (data.value as {data: {accountId: string, title: string, balance: number, type: string} }).data 
    let result = ref<AccountType>({id: resData.accountId, title: resData.title, balance: resData.balance, type: resData.type})

    return ref(result)
}

export type CreateAccountRequest = {
    accountName: string
    accountType: string
}
export async function useFetchCreateAccount(request: CreateAccountRequest){
    const {data, error} = await useFetch('http://localhost:5001/v1/accounts', {
        method: 'POST',
        body: {
            title: request.accountName,
            type: request.accountType
        }
    })

    const toast = useToast();
    if (error.value) {
        const resError = error.value.cause as {field: string, message: string}
        console.log(resError)
        toast.add({ title: 'Oops! Erreur', description: resError.message, color: 'error'})
        return
    }

    const resData = data.value as {data: {accountId: string}}
    toast.add({ title: 'Success', description: `Nouveau compte ajoute ID:${resData.data.accountId}`, color: 'success'})
}

export type UpdateAccountRequest = {
    accountId: string
    accountName: string
    accountType: string
}

export async function useFetchUpdateAccount(request: UpdateAccountRequest) {
    const {data, error} = await useFetch(`http://localhost:5001/v1/accounts/${request.accountId}`, {
        method: 'PUT',
        body: {
            title: request.accountName,
            type: request.accountType
        }
    })

    const toast = useToast();
    if (error.value) {
        console.log(request.accountName)
        const resError = error.value.data.error as {field: string, message: string}
        console.log(error)
        toast.add({ title: 'Oops! Erreur', description: resError.message, color: 'error'})
        return
    }

    toast.add({ title: 'Success', description: `Compte mis a jour ID:${request.accountId}`, color: 'success'})
}

export async function useFetchDeleteAccont(accountId: string) {
    const {data, error} = await useFetch(`http://localhost:5001/v1/accounts/${accountId}`, {
        method: 'DELETE'
    })

    const toast = useToast();
    if (error.value) {
        const resError = error.value.cause as {field: string, message: string}
        console.log(resError)
        toast.add({ title: 'Oops! Erreur', description: resError.message, color: 'error'})
        return
    }

    toast.add({ title: 'Success', description: `Compte ID:${accountId} supprime`, color: 'success'})
}