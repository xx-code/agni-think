import type { CreatedRequest } from "~/types/api";
import type { CreateInvoiceRequest } from "~/types/api/transaction";

export default async function useCreateInvoice(request: CreateInvoiceRequest): Promise<CreatedRequest> {
    const created = await $fetch<CreatedRequest>('/api/invoices', {
        method: 'POST',
        body: request
    });

    return created;
}