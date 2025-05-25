export const ALL_ACCOUNT_ID = 'all';
export type ResumeAccountType = {
    id: string,
    title: string,
    balance: number,
    typeAccount: string,
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

export const useFetchResumeAccount = (): Ref<ResumeAccountType[]> => {
    const accounts: ResumeAccountType[] = [
        {
            id: 'acc-1',
            title: 'Principal',
            balance: 1500,
            pastBalanceDetail: {
                balance: 1425,
                diffPercent: Number((Math.abs(((1425/1500) * 100) - 100)).toFixed(2)),
                doIncrease: true
            },
            typeAccount: 'Checking'
        },
        {
            id: 'acc-2',
            title: 'Secondaire',
            balance: 7500,
            pastBalanceDetail: {
                balance: 8000,
                diffPercent: Number((Math.abs(((8000/7500) * 100) - 100)).toFixed(2)),
                doIncrease: false 
            },
            typeAccount: 'Checking'
        },
        {
            id: 'acc-3',
            title: 'Vehicule',
            balance: 385,
            pastBalanceDetail: {
                balance: 584,
                diffPercent: Number((Math.abs(((584/385) * 100) - 100)).toFixed(2)),
                doIncrease: false 
            },
            typeAccount: 'Checking'
        },
        {
            id: 'acc-4',
            title: 'Epargne',
            balance: 10751,
            pastBalanceDetail: {
                balance: 10000,
                diffPercent: Number((Math.abs(((10751/10000) * 100) - 100)).toFixed(2)),
                doIncrease: true 
            },
            typeAccount: 'Saving'
        },
    ]
    const totals = sumTotalBalance(accounts)
    const totalAccount: ResumeAccountType= {
        id: ALL_ACCOUNT_ID,
        title: 'Total Balance',
        balance: totals[0],
        pastBalanceDetail: {
            balance: totals[1],
            diffPercent: Number((Math.abs(((10751/10000) * 100) - 100)).toFixed(2)), 
            doIncrease: totals[1] < totals[0]
        },
        typeAccount: ''
    }

    accounts.unshift(totalAccount)

    return ref(accounts)
}

export type AccountType = {
    id: string,
    label: string
}

export const useFetchAccountTypes = (): Ref<AccountType[]> => {
    const accountTypes = ref([
        {
            id: "Checking",
            label: "Debit"
        },
        {
            id: "Saving",
            label: "Epargne"
        },
        {
            id: "Business",
            label: "Business"
        },
        {
            id: "Broking",
            label: "Investissement"
        }
    ])

    return accountTypes;
}

export type NewAccountRequest = {
    accountName: string
    accountType: string
}
export async function addNewAccount(request: NewAccountRequest){
    const toast = useToast();

    toast.add({ title: 'Success', description: 'Nouveau compte ajoute', color: 'success'})

    console.log(request);
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
