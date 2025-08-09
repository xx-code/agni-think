export default async function useDeleteCategory(categoryId: string): Promise<void> {
    await $fetch(`/api/categories/${categoryId}`, {
        method: 'DELETE'
    });
}