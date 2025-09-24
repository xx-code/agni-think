export default async function useDeleteNotification(notificationId: string) {
    await $fetch(`/api/notifications/${notificationId}`, {
        method: 'DELETE'
    })
} 