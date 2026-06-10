import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { CreateBankRegisterRequest, GetBankRegisterResponse } from "~/types/api/bank-register";
import type { BankRegisterType } from "~/types/ui/bank-register";

export async function createBankRegister(request: CreateBankRegisterRequest): Promise<CreatedRequest> {
    const res = await $fetch<CreatedRequest>(`${getApiBase()}/bank-registers`, {
        method: 'POST',
        body: request
    })

    return res
}

export async function fetchAllBankRegister(query: QueryFilterRequest): Promise<ListResponse<BankRegisterType>> {
    const res = await $fetch<ListResponse<GetBankRegisterResponse>>(`${getApiBase()}/bank-registers`, {
        method: 'GET',
        query: query
    })

    return {
        items: res.items.map(i => ({
            id: i.bankRegisterId,
            title: i.title,
            active: i.isActive,
            accounts: i.accounts.map(x => ({ accountId: x.accountId, bankAccountId: x.bankAccountId, accountName: x.accountName}))
        } satisfies BankRegisterType)),
        total: res.total
    }
}