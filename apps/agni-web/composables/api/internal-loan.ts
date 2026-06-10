import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { CreateInternalLoanRequest, GetInternalLoanResponse, UpdateInternalLoanRequest } from "~/types/api/internal-loan";
import type { InternalLoanType } from "~/types/ui/internal-loan";

export async function fetchInternalLoan(id: string): Promise<InternalLoanType> {
    const res = await $fetch<GetInternalLoanResponse>(`${getApiBase()}/internal-loans/${id}`, {
        method: 'GET'
    })

    return {
        id: res.id,
        fundSourceId: res.fundSourceId,
        invoiceId: res.invoiceId,
        creditTargetId: res.creditTargetId,
        dueDate: new Date(res.dueDate) 
    }
}

export async function fetchInternalLoans(query: QueryFilterRequest): Promise<ListResponse<InternalLoanType>> {
    const res = await $fetch<ListResponse<GetInternalLoanResponse>>(`${getApiBase()}/internal-loans`, {
        method: 'GET',
        query: query
    })

    return {
        items: res.items.map(i => ({
            id: i.id,
            fundSourceId: i.fundSourceId,
            invoiceId: i.invoiceId,
            creditTargetId: i.creditTargetId,
            dueDate: new Date(i.dueDate)
        } satisfies InternalLoanType)),
        total: res.total
    }
}

export async function useCreateInternalLoan(request: CreateInternalLoanRequest): Promise<CreatedRequest> {
    const res = await $fetch<CreatedRequest>(`${getApiBase()}/internal-loans`, {
        method: 'POST',
        body: request
    })

    return res
}

export async function useUpdateInternalLoan(id: string, request: UpdateInternalLoanRequest) {
    await $fetch(`${getApiBase()}/internal-loans/${id}`, {
        method: 'PUT',
        body: request
    })
}

export async function useDeleteInternalLoan(id: string) {
    await $fetch(`${getApiBase()}/internal-loans/${id}`, {
        method: 'DELETE',
    })
}