import type { ListResponse } from "~/types/api";
import type { GetAllAccountResponse } from "~/types/api/account";
import type { AccountType } from "~/types/ui/account";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useAccounts(): UseApiFetchReturn<ListResponse<AccountType>> {
    const { data, error, refresh } = useFetch('/api/accounts', {
        method: 'GET',
        transform: (data: ListResponse<GetAllAccountResponse>) => {
            return {
                items: data.items.map(i => ({
                    id: i.accountId,
                    title: i.title,
                    balance: i.balance,
                    type: i.type
                })),
                totals: data.totals
            } satisfies ListResponse<AccountType>;
        }
    });

    return { data, error, refresh };
}