import type { ListResponse } from "~/types/api";
import type { GetAllAccountResponse } from "~/types/api/account";

export default function useAccounts(): UseApiFetchReturn<ListResponse<GetAllAccountResponse>> {
    const { data, error, refresh } = useFetch('api/accounts');

    return { data, error, refresh };
}