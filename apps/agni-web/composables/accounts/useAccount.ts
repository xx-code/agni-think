import type { GetAccountResponse, GetAccountWithDetailResponse } from "~/types/api/account";
import type { AccountType, AccountWithDetailType } from "~/types/ui/account";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useAccount(accountId: string): UseApiFetchReturn<AccountType> {
    const { data, error, refresh } = useFetch(`/api/accounts/${accountId}`, {
        method: 'GET',
        transform: (data: GetAccountResponse) => {
            return {
                id: data.accountId,
                title: data.title,
                balance: data.balance,
                type: data.type
            } satisfies AccountType
        }
    });

    return {data, error, refresh};
}


export async function fetchAccount(accountId: string): Promise<AccountType> {
    const res = await $fetch<GetAccountResponse>(`/api/accounts/${accountId}`, {
        method: 'GET'
    });
    return {
        id: res.accountId,
        title: res.title,
        balance: res.balance,
        type: res.type
    };
}

export async function fetchAccountWithDetail(accountId: string): Promise<AccountWithDetailType> {
    const res = await $fetch<GetAccountWithDetailResponse>(`/api/accounts/${accountId}/getAccountWithDetail`, {
        method: 'GET'
    });
    return {
        id: res.accountId,
        title: res.title,
        balance: res.balance,
        type: res.type,
        lockedBalance: res.lockedBalance,
        freezedBalance: res.freezedBalance
    };
}