import type { UpdateCategoryRequest } from "~/types/api/category";

export default async function useUpdateCategory(categoryId: string, request: UpdateCategoryRequest): Promise<void> {
    await $fetch(`/api/categories/${categoryId}`, {
        method: 'PUT',
        body: request
    })
}