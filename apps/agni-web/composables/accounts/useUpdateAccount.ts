export default async function useUpdateAccount(accountId: string, request: UpdateAccountRequest): Promise<void> {
    await $fetch(`api/accounts/${accountId}`, {
        method: 'PUT',
        body: request
    });
}