import type { ListResponse } from "~/types/api";
import type { GetAccountResponse } from "~/types/api/account";
import type { FilterBalanceTransactionQuery, GetBalanceResponse } from "~/types/api/transaction";
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

export default function useAccountsWitPastBalance(dateStart: Date, dateEnd: Date): 
UseApiFetchReturn<ListResponse<AccountWithPastBalanceType>> {

    const { data, error, refresh } = useAsyncData('resume-accounts', async () => { 

        const accounts = await $fetch<ListResponse<GetAccountResponse>>('/api/accounts', {
            method: "GET"
        });

        const accountsWithPastBalances: AccountWithPastBalanceType[] = [];

        for(const account of accounts.items) {
            const requestBalance: FilterBalanceTransactionQuery = {
                accountFilterIds: [account.accountId],
                dateStart: dateStart.toLocaleString(),
                dateEnd: dateEnd.toLocaleString()
            };

            const accountBalance = await $fetch<GetBalanceResponse>('/api/transactions/balance', {
                method: 'POST',
                body: requestBalance 
            });

            const pastBalance = accountBalance.balance;
            
            accountsWithPastBalances.push({
                id: account.accountId,
                title: account.title,
                balance: account.balance,
                type: account.type,
                pastBalanceDetail: {
                    balance: pastBalance,
                    diffPercent: computeDiffPercent(pastBalance, account.balance),
                    doIncrease: pastBalance > account.balance 
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

        return { items: accountsWithPastBalances, totals: accounts.totals};
    });

    return { data, error, refresh };
}