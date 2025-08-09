import type { GetAccountResponse } from "~/types/api/account";
import type { AccountType } from "~/types/ui/account";
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