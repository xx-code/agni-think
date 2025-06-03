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

export const useFetchResumeAccount = async (): Promise<Ref<ResumeAccountType[]>> => {

    const accounts = await useFetchListAccount();
    const resumeAccounts: ResumeAccountType[] = []
    for(const account of accounts.value) {
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

    return ref(resumeAccounts)
}

export const useFetchListAccount = async (): Promise<Ref<AccountType[]>> => {
    const {data, error} = await useFetch('http://localhost:5001/v1/accounts')
    if (error.value) {
        const toast = useToast()
        toast.add({title: 'Oops! Erreur type de compte', description: error.value.data, color: 'error'})
        return ref([])
    }

    let results = ref<AccountType[]>((data.value as {data: {accountId: string, title: string, balance: number, type: string} }[]).data.map(val => ({id: val.accountId, title: val.title, balance: val.balance, type: val.type})))

    return ref(results)
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
export async function updateAccount(request: UpdateAccountRequest){
    const toast = useToast()

    toast.add({ title: 'Success', description: 'Mise a jour de compte', color: 'success'})

    console.log(request)
}
