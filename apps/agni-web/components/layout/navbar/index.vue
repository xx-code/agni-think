<script lang="ts" setup>
import type { NuxtError } from '#app'
import { SlideOverNotification } from '#components'
import { useCheckNotifications } from '~/composables/api/notifications'
import type { ErrorResponse } from '~/types/api'

const overlay = useOverlay()
const slideOverNotification = overlay.create(SlideOverNotification)

const isLoading = ref(false)
const hasNotifications = ref(false)
const notificationError = ref<ErrorResponse>()

async function refreshNotification() {
    isLoading.value = true
    const res = await useCheckNotifications()      
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
        <div class="flex justify-between lg:justify-end   items-center">
            <LayoutNavbarNotificationIcon 
                @on-open="openNotificationHub"
                :disabled="isLoading"
                :has-notifications="hasNotifications"
            />
            <UDashboardSidebarToggle />
        </div>
    </div>
</template>