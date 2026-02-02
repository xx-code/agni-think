<script setup lang="ts">
import { SlideOverNotification } from '#components'
import { checkNotifications } from '~/composables/notifications/useNotifications'

const overlay = useOverlay()
const slideOverNotification = overlay.create(SlideOverNotification)
const isNotif = ref(false)
const isLoading = ref(false)

async function openNotificationHub() {
  try {
    const instance = slideOverNotification.open({
      onClose: refreshNotification
    })
    await instance.result
    await refreshNotification()
  } catch (error) {
    console.error('Failed to open notifications:', error)
  }
}

async function refreshNotification() {
  try {
    isLoading.value = true
    const res = await checkNotifications()
    isNotif.value = res
  } catch (error) {
    console.error('Failed to check notifications:', error)
  } finally {
    isLoading.value = false
  }
}

onMounted(async () => {
  await refreshNotification()
  
  // Optional: Auto-refresh notifications every 5 minutes
  const interval = setInterval(refreshNotification, 300000)
  onUnmounted(() => clearInterval(interval))
})
</script>

<template>
  <button 
    class="notification-button"
    @click="openNotificationHub"
    :disabled="isLoading"
    :aria-label="isNotif ? 'You have new notifications' : 'No new notifications'"
  >
    <UChip :show="isNotif" color="error" size="sm">
      <UIcon 
        name="i-lucide-bell" 
        size="xl"
        :class="{ 'bell-shake': isNotif }"
      />
    </UChip>
  </button>
</template>

<style scoped lang="scss">
.notification-button {
  background: rgba(103, 85, 215, 0.08);
  border: none;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 48px;
  width: 48px;
  cursor: pointer;
  transition: all 250ms cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;

  &:hover {
    background: rgba(103, 85, 215, 0.15);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(103, 85, 215, 0.2);
  }

  &:active {
    transform: translateY(0);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  &:focus-visible {
    outline: 2px solid #6755d7;
    outline-offset: 2px;
  }
}

.bell-shake {
  animation: bellRing 1s ease-in-out infinite;
}

@keyframes bellRing {
  0%, 100% {
    transform: rotate(0deg);
  }
  10%, 30% {
    transform: rotate(-10deg);
  }
  20%, 40% {
    transform: rotate(10deg);
  }
  50% {
    transform: rotate(0deg);
  }
}
</style>