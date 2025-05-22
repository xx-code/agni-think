export type ResumeAccountType = {
    id: string,
    title: string,
    balance: number,
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
            }
        },
        {
            id: 'acc-2',
            title: 'Secondaire',
            balance: 7500,
            pastBalanceDetail: {
                balance: 8000,
                diffPercent: Number((Math.abs(((8000/7500) * 100) - 100)).toFixed(2)),
                doIncrease: false 
            }
        },
        {
            id: 'acc-3',
            title: 'Vehicule',
            balance: 385,
            pastBalanceDetail: {
                balance: 584,
                diffPercent: Number((Math.abs(((584/385) * 100) - 100)).toFixed(2)),
                doIncrease: false 
            }
        },
        {
            id: 'acc-4',
            title: 'Epargne',
            balance: 10751,
            pastBalanceDetail: {
                balance: 10000,
                diffPercent: Number((Math.abs(((10751/10000) * 100) - 100)).toFixed(2)),
                doIncrease: true 
            }
        },
    ]
    const totals = sumTotalBalance(accounts)
    const totalAccount: ResumeAccountType = {
        id: 'all',
        title: 'Total Balance',
        balance: totals[0],
        pastBalanceDetail: {
            balance: totals[1],
            diffPercent: Number((Math.abs(((10751/10000) * 100) - 100)).toFixed(2)), 
            doIncrease: totals[1] < totals[0]
        }
    }

    accounts.unshift(totalAccount)

    return ref(accounts)
}