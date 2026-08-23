import type { ListResponse } from "~/types/api";
import type { GetInternalLoanResponse } from "~/types/api/internal-loan";
import type { InternalLoanType } from "~/types/ui/internal-loan";

export function internalLoanResponseToInternalLoan(data: GetInternalLoanResponse): InternalLoanType {
    return {
        id: data.id,
        fundSourceId: data.fundSourceId,
        creditTargetId: data.creditTargetId,
        invoiceId: data.invoiceId,
        dueDate: new Date(data.dueDate),
        freezeInvoices: data.freezeInvoices,
        loanAmount: data.loanAmount,
        refundAmount: data.refundAmount
    }
}

export function listInternalLoansResponseToListInternalLoans(data: ListResponse<GetInternalLoanResponse>): ListResponse<InternalLoanType> {
    return {
        items: data.items.map(i => internalLoanResponseToInternalLoan(i)),
        total: data.total
    }
}
