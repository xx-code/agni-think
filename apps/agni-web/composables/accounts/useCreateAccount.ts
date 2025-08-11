import type { CreatedRequest } from "~/types/api";
import type { CreateAccountRequest } from "~/types/api/account";

export default async function useCreateAccount(request: CreateAccountRequest): Promise<CreatedRequest> {
    const response = await $fetch('/api/accounts', {
        method: 'POST',
        body: request
    });

    return (response as CreatedRequest);
}