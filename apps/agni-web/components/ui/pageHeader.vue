<script setup lang="ts">
const props = defineProps<{
    title: string
    button?: {
        icon: string
        label: string
    }
    subtitle?: string
}>()

const emit = defineEmits<{
    clickButton: []
}>()
</script>

<template>
    <div class="flex justify-between items-center">
        <div>
            <h1 class="text-3xl font-bold tracking-tight text-gray-900">{{ title }}</h1>
            <p v-if="subtitle" class="text-sm text-gray-500 mt-1">{{ subtitle }}</p>
        </div>

        <UButton 
            v-if="button"
            :icon="button.icon" 
            size="lg" 
            variant="solid" 
            color="primary"
            @click="emit('clickButton')">
            {{ button.label }}
        </UButton>
    </div>
</template>