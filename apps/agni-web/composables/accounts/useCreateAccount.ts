import type { CreatedRequest } from "~/types/api";

export default async function useCreateAccount(request: CreateAccountRequest): Promise<CreatedRequest> {
    const response = await $fetch('api/accounts', {
        method: 'POST',
        body: request
    });

    return (response as CreatedRequest);
}