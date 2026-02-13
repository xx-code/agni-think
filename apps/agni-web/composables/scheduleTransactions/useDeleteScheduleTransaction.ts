export default async function useDeleteScheduleInvoice(scheduleTransactionId: string): Promise<void> {
    await $fetch(`/api/schedule-invoices/${scheduleTransactionId}`, {
        method: 'DELETE'
    });
}