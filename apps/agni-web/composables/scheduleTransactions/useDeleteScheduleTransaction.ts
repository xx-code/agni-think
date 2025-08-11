export default async function useDeleteScheduleTransaction(scheduleTransactionId: string): Promise<void> {
    await $fetch(`/api/schedule-transactions/${scheduleTransactionId}`, {
        method: 'DELETE'
    });
}