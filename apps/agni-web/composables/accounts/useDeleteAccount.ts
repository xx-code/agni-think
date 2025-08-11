export default async function useDeleteAccount(accountId: string): Promise<void> {
    await $fetch(`/api/accounts/${accountId}`, {
        method: 'DELETE'
    });
}