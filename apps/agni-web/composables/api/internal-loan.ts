import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { AddRefundInternalRequest, CreateInternalLoanRequest, GetInternalLoanResponse, RemoveInternalRequestRequest, UpdateInternalLoanRequest } from "~/types/api/internal-loan";
import type { InternalLoanType } from "~/types/ui/internal-loan";

export async function fetchInternalLoan(id: string): Promise<InternalLoanType> {
    const res = await $fetch<GetInternalLoanResponse>(`api/internal-loans/${id}`, {
        method: 'GET'
    })

    return {
        id: res.id,
        fundSourceId: res.fundSourceId,
        invoiceId: res.invoiceId,
        creditTargetId: res.creditTargetId,
        dueDate: new Date(res.dueDate), 
        freezeInvoices: res.freezeInvoices,
        loanAmount: res.loanAmount,
        refundAmount: res.refundAmount
    }
}

export async function fetchInternalLoans(query: QueryFilterRequest): Promise<ListResponse<InternalLoanType>> {
    const res = await $fetch<ListResponse<GetInternalLoanResponse>>(`api/internal-loans`, {
        method: 'GET',
        query: query
    })

    return {
        items: res.items.map(i => ({
            id: i.id,
            fundSourceId: i.fundSourceId,
            invoiceId: i.invoiceId,
            creditTargetId: i.creditTargetId,
            dueDate: new Date(i.dueDate),
            freezeInvoices: i.freezeInvoices,
            loanAmount: i.loanAmount,
            refundAmount: i.refundAmount 
        } satisfies InternalLoanType)),
        total: res.total
    }
}

export async function useCreateInternalLoan(request: CreateInternalLoanRequest): Promise<CreatedRequest> {
    const res = await $fetch<CreatedRequest>(`api/internal-loans`, {
        method: 'POST',
        body: request
    })

    return res
}

export async function useUpdateInternalLoan(id: string, request: UpdateInternalLoanRequest) {
    await $fetch(`api/internal-loans/${id}`, {
        method: 'PUT',
        body: request
    })
}

export async function useDeleteInternalLoan(id: string) {
    await $fetch(`api/internal-loans/${id}`, {
        method: 'DELETE',
    })
}

export async function useAddRefundInternalLoan(id: string, request: AddRefundInternalRequest) {
    await $fetch(`api/internal-loans/${id}/add-fund`, {
        method: 'PUT',
        body: request
    })
}

export async function useRemoveRefundInternalLoan(id: string, request: RemoveInternalRequestRequest) {
    await $fetch(`api/internal-loans/${id}/remove-fund`, {
        method: 'PUT',
        body: request
    })
}