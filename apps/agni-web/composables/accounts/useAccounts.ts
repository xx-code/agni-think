import type { Reactive } from "vue";
import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetAccountResponse, GetAccountWithDetailResponse } from "~/types/api/account";
import type { AccountBrokeDetailType, AccountCheckingDetailType, AccountCreditDetailType, AccountType, AccountWithDetailType } from "~/types/ui/account";
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

export async function fetchAccounts(query: QueryFilterRequest): Promise<ListResponse<AccountType>> {
    const res = await $fetch<ListResponse<GetAccountResponse>>('/api/accounts', {
        method: 'GET',
        query: query,
    });

    return {
        items: res.items.map(i => ({
            id: i.id,
            title: i.title,
            balance: i.balance,
            type: i.type
        } satisfies AccountType)),
        total: Number(res.total) 
    }
} 

export async function fetchAccountsWithDetail(query: QueryFilterRequest): Promise<ListResponse<AccountWithDetailType>> {
    const res = await $fetch<ListResponse<GetAccountWithDetailResponse>>('/api/accounts/getAllAccountWithDetail', {
        method: 'GET',
        query: query
    })

    const accountsWithPastBalances: AccountWithDetailType[] = []; 

    for(const account of res.items) { 
        const detailAccount = (type: string) => {
            switch(type) {
                case 'Broking':
                    return {
                        managementType: account.detail.detailForBroking?.contributionType ?? "",
                        type: account.detail.detailForBroking?.managementType ?? ""
                    } satisfies AccountBrokeDetailType 
                case 'CreditCard':    
                    return {
                        creditUtilisation: account.detail.detailForCreditCard?.creditUtilisation ?? 0,
                        creditLimit: account.detail.detailForCreditCard?.creditCardLimit ?? 0
                    } satisfies AccountCreditDetailType
                case 'Checking':
                    return {
                        buffer: account.detail.detailForChecking?.buffer ?? 0
                    } satisfies AccountCheckingDetailType
                default:
                    return undefined
            }
        }

        accountsWithPastBalances.push({
            id: account.id,
            title: account.title,
            balance: account.balance,
            type: account.type,
            lockedBalance: account.lockedBalance, 
            freezedBalance: account.freezeBalance,
            detail: detailAccount(account.type)
        });            
    }

    const totals = sumTotalBalance(accountsWithPastBalances);
    const totalAccount: AccountWithDetailType = {
        id: ALL_ACCOUNT_ID,
        title: 'Total Balance',
        balance: totals[0],
        lockedBalance: totals[1],
        freezedBalance: totals[2],
        type: '',
        detail: undefined
    }

    accountsWithPastBalances.unshift(totalAccount)

    return { items: accountsWithPastBalances, total: res.total}; 
} 