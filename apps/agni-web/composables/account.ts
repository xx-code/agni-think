// export const ALL_ACCOUNT_ID = 'all';

// export type AccountType = {
//     id: string
//     title: string
//     balance: number
//     type: string
// }

// export type ResumeAccountType = AccountType & {
//     pastBalanceDetail: {
//         balance: number,
//         diffPercent: number,
//         doIncrease: boolean
//     }
// }

// function sumTotalBalance(accounts: ResumeAccountType[]): [number, number] {
//     let total = 0 
//     let pastTotal = 0
//     accounts.forEach(acc => {
//         if (acc.type !== 'Saving' && acc.type !== 'Broking')
//             total += acc.balance 
//             pastTotal += acc.pastBalanceDetail.balance
//     }) 

//     return [Number(total.toFixed(2)), Number(pastTotal.toFixed(2))]
// }

// function computeDiffPercent(pastBalance: number, balance: number) {
//     if (pastBalance == 0)
//         return 0
//     return Number((Math.abs(((balance/pastBalance) * 100) - 100)).toFixed(2))
// }

// export const useFetchResumeAccount = (): UseApiFetchReturn<ResumeAccountType[]> => {
//     const { data, error, refresh } = useAsyncData('resume-accounts', async () => {
    
//         const data = await fetchListAccounts();
//         const resumeAccounts: ResumeAccountType[] = []

//         // generate code
//         const now = new Date();
//         const prevMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1);

//         const startDate = new Date(prevMonth.getFullYear(), prevMonth.getMonth(), 1)
//         .toISOString()
//         .split('T')[0];

//         const endDate = new Date(prevMonth.getFullYear(), prevMonth.getMonth() + 1, 0)
//         .toISOString()
//         .split('T')[0];
//         // 

//         for(const account of data) {
//             let pastBalance = await fetchBalance({accountIds: [account.id], dateStart: startDate, dateEnd: endDate})
//             resumeAccounts.push({
//                 ...account, 
//                 pastBalanceDetail: {
//                     balance: pastBalance, 
//                     diffPercent: computeDiffPercent(pastBalance, account.balance), 
//                     doIncrease: true
//                 }}
//             )
//         } 

//         const totals = sumTotalBalance(resumeAccounts)
//         const totalAccount: ResumeAccountType= {
//             id: ALL_ACCOUNT_ID,
//             title: 'Total Balance',
//             balance: totals[0],
//             pastBalanceDetail: {
//                 balance: totals[1],
//                 diffPercent: computeDiffPercent(totals[1], totals[0]), 
//                 doIncrease: totals[1] < totals[0]
//             },
//             type: ''
//         }

//         resumeAccounts.unshift(totalAccount)

//         return resumeAccounts
//     })

//     if (error.value) {
//         const toast = useToast()
//         const resError = error as Ref<ErrorApi>
//         toast.add({title: 'Oops! Erreur type de compte', description: resError.value.data.error.message, color: 'error'})
//         data.value = []
//         return {data: data as Ref<[]>, error: resError, refresh}
//     }
     
//     return {data: data as Ref<ResumeAccountType[]>, error: error as Ref<null>, refresh } 
// }

// export type AccountTypeType = {
//     id: string,
//     label: string
// }
// export const useFetchListAccountTypes = (): UseApiFetchReturn<AccountTypeType[]|null> => { 
//     const { data, error, refresh } = useAsyncData(() => fetchListAccountTypes())

//     if (error.value) {
//         const toast = useToast()
//         const resError = error as Ref<ErrorApi>
//         toast.add({title: 'Oops! Erreur type de compte', description: resError.value.data.error.message, color: 'error'})
//         data.value = []
//         return {data: data  as Ref<[]>, error: resError, refresh}
//     }
    
//     return {data: data as Ref<AccountTypeType[]>, error: error as Ref<null>, refresh}
// }

// export const useFetchListAccount = (): UseApiFetchReturn<AccountType[]|null> => {
//     const { data, error, refresh } = useAsyncData('accounts', () => fetchListAccounts())
//     if (error.value) {
//         const toast = useToast()
//         const resError = error as Ref<ErrorApi>
//         toast.add({title: 'Oops! Erreur type de compte', description: resError.value.data.error.message, color: 'error'})
//         data.value = []
//         return {data, error: resError, refresh}
//     }

//     return { data, error: error as Ref<ErrorApi>, refresh }
// }

// export const useFetchAccount = (accountId: string): UseApiFetchReturn<AccountType|null> => {
//     const {data, error, refresh} = useAsyncData('account', () => fetchAccount(accountId))

//     if (error.value) {
//         const toast = useToast()
//         const resError = error as Ref<ErrorApi> 
//         toast.add({title: 'Oops! Erreur type de compte', description: resError.value.data.error.message, color: 'error'})

//         return {data, error: resError, refresh}
//     }

//     return {data, error: error as Ref<null>, refresh}
// }

// export async function fetchListAccountTypes(): Promise<AccountTypeType[]> {
//     try {
//         const api = API_LINK()
//         const response = await $fetch(`${api}/internal/account-type`)
//         const data = (response as {id: string, value: string}[])

//         return data.map(value => ({id: value.id, label: value.value}))
//     } catch(err) {
//         console.log(err)
//         return []
//     }
// }

// export async function fetchListAccounts(): Promise<AccountType[]> {
//     try {
//         const api = API_LINK()
//         const response = await $fetch(`${api}/accounts`)
//         const data = (response as {data: {accountId: string, title: string, balance: number, type: string}[]}).data

//         return data.map(value => ({id: value.accountId, title: value.title, balance: value.balance, type: value.type}))
//     } catch(err) {
//         console.log(err)
//         return []
//     }
// }

// export async function fetchAccount(accountId: string): Promise<AccountType|null>{
//     try {
//         const api = API_LINK()
//         const response = await $fetch(`${api}/accounts/${accountId}`)
//         const data = (response as {data: {accountId: string, title: string, balance: number, type: string}}).data

//         return {id: data.accountId, title: data.title, balance: data.balance, type: data.type}
//     } catch(err) {
//         console.log(err)
//         return null
//     }
// }

// export type CreateAccountRequest = {
//     accountName: string
//     accountType: string
// }
// export async function fetchCreateAccount(request: CreateAccountRequest): Promise<void>{
//     const toast = useToast();
//     try {
//         const api = API_LINK()
//         const response = await $fetch(`${api}/accounts`, {
//             method: 'post',
//             body: {
//                 title: request.accountName,
//                 type: request.accountType
//             }
//         })

//         const data = response as {success: boolean, data: {accountId: string}}

//         if (!data.success) {
//             toast.add({ title: 'Oops! Erreur', description: "", color: 'error'})
//         }

//         toast.add({ title: 'Success', description: `Compte ${request.accountName} cree`, color: 'success' })
//     } catch (err) {
//         const resData = err as ErrorApi
//         toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
//     }
// }

// // export type UpdateAccountRequest = {
// //     accountName: string
// //     accountType: string
// // }
// export async function fetchUpdateAccount(request: UpdateAccountRequest): Promise<void> {
//     const toast = useToast();
//     try {
//         const api = API_LINK()
//         const response = await $fetch(`${api}/accounts/${request.accountId}`, {
//             method: 'PUT',
//             body: {
//                 title: request.accountName,
//                 type: request.accountType
//             }
//         })

//         const data = response as {success: boolean}

//         if (!data.success) {
//             toast.add({ title: 'Oops! Erreur', description: "", color: 'error'})
//         }

//         toast.add({ title: 'Success', description: `Compte ${request.accountName} mettre a jour`, color: 'success'})
//     } catch(err) {
//         const resData = err as ErrorApi
        
//         toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
//     }
    
// }

// export async function fetchDeleteAccount(accountId: string): Promise<boolean> {
//     const api = API_LINK()
//     const response = await $fetch(`${api}/accounts/${accountId}`, {
//         method: 'DELETE'
//     })

//     const data = response as {success: boolean}
//     return data.success 
// }
