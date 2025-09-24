import type { Reactive } from "vue";
import type { ListResponse } from "~/types/api";
import type { GetAllAccountPastBalanceRequest, GetAllAccountWithPastBalanceResponse } from "~/types/api/account";
import type { AccountWithPastBalanceType } from "~/types/ui/account";
import type { UseApiFetchReturn } from "~/types/utils";

export const ALL_ACCOUNT_ID = "all"

function sumTotalBalance(accounts: AccountWithPastBalanceType[]): [number, number] {
    let total = 0;
    let pastTotal = 0;
    accounts.forEach(acc => {
        if (acc.type !== 'Saving' && acc.type !== 'Broking')
            total += acc.balance 
            pastTotal += acc.pastBalanceDetail.balance
    });

    return [Number(total.toFixed(2)), Number(pastTotal.toFixed(2))];
}

function computeDiffPercent(pastBalance: number, balance: number) {
    if (pastBalance == 0)
        return 0
    return Number((Math.abs(((balance/pastBalance) * 100) - 100)).toFixed(2))
}

export default function useAccountsWitPastBalance(request: Reactive<GetAllAccountPastBalanceRequest>): 
UseApiFetchReturn<ListResponse<AccountWithPastBalanceType>> {

    const { data, error, refresh } = useFetch('/api/accounts/getAllAccountWithBalance', {
        method: 'GET',
        query: request,
        transform: (data: ListResponse<GetAllAccountWithPastBalanceResponse>) => {
            const accountsWithPastBalances: AccountWithPastBalanceType[] = [];

            for(const account of data.items) { 
                accountsWithPastBalances.push({
                    id: account.accountId,
                    title: account.title,
                    balance: account.balance,
                    type: account.type,
                    pastBalanceDetail: {
                        balance: account.pastBalance,
                        diffPercent: computeDiffPercent(account.pastBalance, account.balance),
                        doIncrease: account.pastBalance > account.balance 
                    }
                });            
            }

            const totals = sumTotalBalance(accountsWithPastBalances);
            const totalAccount: AccountWithPastBalanceType = {
                id: ALL_ACCOUNT_ID,
                title: 'Total Balance',
                balance: totals[0],
                pastBalanceDetail: {
                    balance: totals[1],
                    diffPercent: computeDiffPercent(totals[1], totals[0]), 
                    doIncrease: totals[1] < totals[0]
                },
                type: ''
            }

            accountsWithPastBalances.unshift(totalAccount)

            return { items: accountsWithPastBalances, totals: data.totals};
        }
    }); 

    return { data, error, refresh };
}