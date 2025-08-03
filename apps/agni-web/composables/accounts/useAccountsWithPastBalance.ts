import type { ListResponse } from "~/types/api";
import type { GetAccountResponse, GetAccountWithPastInfoResponse } from "~/types/api/account";
import type { FilterBalanceTransactionQuery, GetBalanceResponse } from "~/types/api/transaction";

export const ALL_ACCOUNT_ID = "all"

function sumTotalBalance(accounts: GetAccountWithPastInfoResponse[]): [number, number] {
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
UseApiFetchReturn<ListResponse<GetAccountWithPastInfoResponse>> {
    const { data, error, refresh } = useAsyncData('resume-accounts', async () => { 

        const accounts: ListResponse<GetAccountResponse> = await $fetch('/api/accounts');

        const accountsWithPastBalances: GetAccountWithPastInfoResponse[] = [];

        for(const account of accounts.items) {
            const requestBalance: FilterBalanceTransactionQuery = {
                accountIds: [account.accountId],
                dateStart: dateStart.toLocaleString(),
                dateEnd: dateEnd.toLocaleString()
            };

            const accountBalance: GetBalanceResponse = await $fetch('/api/transactions/balance', {
                method: 'POST',
                body: requestBalance 
            });

            const pastBalance = accountBalance.balance;
            
            accountsWithPastBalances.push({
                ...account,
                pastBalanceDetail: {
                    balance: pastBalance,
                    diffPercent: computeDiffPercent(pastBalance, account.balance),
                    doIncrease: pastBalance > account.balance 
                }
            });            
        }

        const totals = sumTotalBalance(accountsWithPastBalances);
        const totalAccount: GetAccountWithPastInfoResponse = {
            accountId: ALL_ACCOUNT_ID,
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

        return accountsWithPastBalances;
    });

    return { data, error, refresh };
}