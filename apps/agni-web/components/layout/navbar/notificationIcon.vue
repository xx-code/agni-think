<script setup lang="ts">
    const { 
        disabled,  
        hasNotifications
    } = defineProps<{
        disabled: boolean
        hasNotifications: boolean
    }>()

    const emit = defineEmits(['onOpen'])
</script>

<template>
    <button 
        class="notification-button"
        @click="emit('onOpen')"
        :disabled="disabled"
        :aria-label="hasNotifications ? 'Vous avez notifications' : 'Aucunes notifications'"
        >
        <UChip :show="hasNotifications" color="error" size="sm">
            <UIcon 
                name="i-lucide-bell" 
                size="sm"
                :class="{ 'bell-shake': hasNotifications }"
            />
        </UChip>
    </button>
</template>

<style scoped lang="scss">
.notification-button {
    background-color: var(--color-neutral-50);
    border: none;
    border-radius: 12px;
    display: flex;
    justify-content: center;
    align-items: center;
    height: 25px;
    width: 25px;
    cursor: pointer;
    transition: all 250ms cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;

    &:hover {
        background: var(--color-primary-100);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px var(--color-primary-50);
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