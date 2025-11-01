import type { Reactive } from "vue";
import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetAllAccountResponse, GetallAccountWithDetailResponse } from "~/types/api/account";
import type { AccountType, AccountWithDetailType } from "~/types/ui/account";
import type { UseApiFetchReturn } from "~/types/utils";

export const ALL_ACCOUNT_ID = "all"

function sumTotalBalance(accounts: AccountWithDetailType[]): [number, number, number] {
    let total = 0;
    let totalFreezed = 0
    let totalLocked = 0
    accounts.forEach(acc => {
        if (acc.type !== 'Saving' && acc.type !== 'Broking') {
            total += acc.balance  
        }
        totalFreezed += acc.freezedBalance
        totalLocked += acc.lockedBalance
    });

    return [Number(total.toFixed(2)), Number(totalLocked.toFixed(2)), Number(totalFreezed.toFixed(2))];
}

export default function useAccounts(query: Reactive<QueryFilterRequest>): UseApiFetchReturn<ListResponse<AccountType>> {
    const { data, error, refresh } = useFetch('/api/accounts', {
        method: 'GET',
        query: query,
        transform: (data: ListResponse<GetAllAccountResponse>) => {
            return {
                items: data.items.map(i => ({
                    id: i.accountId,
                    title: i.title,
                    balance: i.balance,
                    type: i.type
                })),
                totals: Number(data.totals) 
            } satisfies ListResponse<AccountType>;
        }
    });

    return { data, error, refresh };
}

export async function fetchAccountsWithDetail(query: QueryFilterRequest): Promise<ListResponse<AccountWithDetailType>> {
    const res = await $fetch<ListResponse<GetallAccountWithDetailResponse>>('/api/accounts/getAllAccountWithDetail', {
        method: 'GET',
        query: query
    })

    const accountsWithPastBalances: AccountWithDetailType[] = [];

    for(const account of res.items) { 
        accountsWithPastBalances.push({
            id: account.accountId,
            title: account.title,
            balance: account.balance,
            type: account.type,
            lockedBalance: account.lockedBalance, 
            freezedBalance: account.freezedBalance
        });            
    }

    const totals = sumTotalBalance(accountsWithPastBalances);
    const totalAccount: AccountWithDetailType = {
        id: ALL_ACCOUNT_ID,
        title: 'Total Balance',
        balance: totals[0],
        lockedBalance: totals[1],
        freezedBalance: totals[2],
        type: ''
    }

    accountsWithPastBalances.unshift(totalAccount)

    return { items: accountsWithPastBalances, totals: res.totals}; 
} 