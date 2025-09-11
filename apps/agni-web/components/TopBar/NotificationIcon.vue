<script setup lang="ts">
import { SlideOverNotification } from '#components';
import { checkNotifications } from '~/composables/notifications/useNotifications';

const overlay = useOverlay()
const slideOverNotificaiton = overlay.create(SlideOverNotification)

const isNotif = ref(false)

async function openNotificationHub() {
    const instance = slideOverNotificaiton.open({
        onClose: refreshNotification
    })

    await instance.result

    refreshNotification()
}

async function refreshNotification() {
    console.log("refres")
    const res = await checkNotifications()
    isNotif.value = res
}

onMounted(async () => {
    await refreshNotification() 
})
</script>


<template>
    <div class="container-notification-icon" @click="openNotificationHub()">
        <UChip :show="isNotif" color="error">
            <UIcon name="i-lucide-bell" size="xl"/>
        </UChip>
    </div>
</template>


<style scoped lang="scss">
.container-notification-icon {
    background: rgba(103, 85, 215, 0.1);
    border-radius: 12px;
    display: flex;
    justify-content: center;
    align-items: center;
    height: 45px;
    width: 45px;
}
</style>