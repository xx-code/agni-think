<script lang="ts" setup>
import type { NuxtError } from '#app'
import { SlideOverNotification } from '#components'
import type { Result } from '~/types'
import type { ErrorResponse, ListResponse } from '~/types/api'
import type { NotificationQueryFilterRequest } from '~/types/api/notification'
import { listNotificationsResponseToListNotifications } from '~/mappers/notification'
import { ApiLinkBuilder } from '~/utils/ApiLinkBuilder'
import { API_ROUTES } from '~/shared/routes'

const overlay = useOverlay()
const slideOverNotification = overlay.create(SlideOverNotification)

const isLoading = ref(false)
const hasNotifications = ref(false)
const notificationError = ref<ErrorResponse>()

async function checkHasUnreadNotifications(): Promise<Result<boolean>> {
    try {
        const query: NotificationQueryFilterRequest = { offset: 0, limit: 0, queryAll: true, isRead: false }
        const res = await ApiLinkBuilder
            .route(API_ROUTES.NOTIFICATIONS.GET_NOTIFICATIONS)
            .query(query)
            .mapper(listNotificationsResponseToListNotifications)
            .execute()
        return { success: true, data: res.items.length > 0 }
    } catch(error: any) {
        return { success: false, error: (error as NuxtError).data as ErrorResponse }
    }
}

async function refreshNotification() {
    isLoading.value = true
    const res = await checkHasUnreadNotifications()      
    if (res.success)
        hasNotifications.value = res.data!
    else
        notificationError.value = res.error
    isLoading.value = false
}

async function openNotificationHub() {
    try {
        const instance = slideOverNotification.open({
            onClose: refreshNotification
        })
        await instance.result
        await refreshNotification()
    } catch (error) {
        const err = error as NuxtError
        notificationError.value = err.data as ErrorResponse
        printLog("Failed to check notifications:", err)
    }
}

onMounted(async () => {
    await refreshNotification()
    const interval = setInterval(refreshNotification, 60000)
    onUnmounted(() => clearInterval(interval))
})
</script>

<template>
    <div class="p-2.5">
        <div class="flex justify-between md:justify-end   items-center">
            <LayoutNavbarNotificationIcon 
                @on-open="openNotificationHub"
                :disabled="isLoading"
                :has-notifications="hasNotifications"
            />
            <UDashboardSidebarToggle 
                :ui="{
                    base: 'md:hidden'
                }"
                class="text-lg" 
            />
        </div>
    </div>
</template>