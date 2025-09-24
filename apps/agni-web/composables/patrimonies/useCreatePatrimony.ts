import type { CreatedRequest } from "~/types/api";
import type { CreatePatrimonyRequest } from "~/types/api/patrimony";

export async function useCreatePatrimony(request: CreatePatrimonyRequest): Promise<CreatedRequest> {
    const newPat = await $fetch<CreatedRequest>('/api/patrimonies', {
        method: 'POST',
        body: request
    })

    return newPat 
}