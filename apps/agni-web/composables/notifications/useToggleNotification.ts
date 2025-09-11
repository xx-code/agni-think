export default async function useToggleNotification(notificationId: string) {
    await $fetch(`/api/notifications/${notificationId}/toggle-read`, {
        method: 'PUT'
    })
} 