import type { GetAccountResponse } from "~/types/api/account";

export default function useAccount(accountId: string): UseApiFetchReturn<GetAccountResponse> {
    const { data, error, refresh } = useFetch(`api/accounts/${accountId}`, {
        method: 'GET'
    });

    return {data, error, refresh};
}