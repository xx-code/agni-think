import type { ListResponse } from "~/types/api";
import type { FilterTransactionQuery, GetAllTransactionResponse } from "~/types/api/transaction";
import type { TransactionType } from "~/types/ui/transaction";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useTransactionPagination(query: FilterTransactionQuery): UseApiFetchReturn<ListResponse<TransactionType>> {

    const { data, error, refresh } = useFetch('/api/transactions/pagination', {
        method: 'POST',
        body: query,
        transform: (data: ListResponse<GetAllTransactionResponse>) => {
            return {
                items: data.items.map(i => ({
                    id: i.transactionId,
                    accountId: i.accountId,
                    amount: i.amount,
                    date: new Date(i.date),
                    description: i.description,
                    recordType: i.recordType,
                    type: i.type,
                    status: i.status,
                    categoryId: i.categoryId,
                    tagIds: i.tagRefs,
                    budgetIds: i.budgets
                })),
                totals: data.totals
            } satisfies ListResponse<TransactionType>
        }
    });

    return { data, error, refresh };
}