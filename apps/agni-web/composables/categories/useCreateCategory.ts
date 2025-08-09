import type { CreatedRequest } from "~/types/api";
import type { CreateCategoryRequest } from "~/types/api/category";

export default async function useCreateCategory(request: CreateCategoryRequest): Promise<CreatedRequest> {
    const created = await $fetch<CreatedRequest>(`/api/categories`, {
        method: 'POST',
        body: request
    });

    return created
}