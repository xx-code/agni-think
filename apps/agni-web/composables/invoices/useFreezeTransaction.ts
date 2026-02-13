import type { CreatedRequest } from "~/types/api";
import type { FreezeInvoiceRequest } from "~/types/api/transaction";

export default async function useFreezeInvoice(request: FreezeInvoiceRequest): Promise<CreatedRequest> {
    const created = await $fetch<CreatedRequest>('/api/invoices/freeze', {
        method: 'POST',
        body: request
    });

    return created;
}