export default async function useDeleteBudget(budgetId: string): Promise<void> {
    await $fetch(`/api/budgets/${budgetId}`, {
        method: 'DELETE'
    });
}