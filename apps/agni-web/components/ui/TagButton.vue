<script setup lang="ts">
const { label, removable = false } = defineProps<{
    label: string,
    removable: boolean
}>()
const emit = defineEmits<{
    remove: []
}>()
</script>

<template>
    <div class="flex items-center gap-1 rounded-full border border-primary-700 bg-primary-100 py-1 pl-3 pr-1 text-primary-700">
        <p class="text-sm font-medium">{{ label }}</p>
        <UButton 
            icon="i-lucide-x"
            variant="ghost"
            :aria-label="`Retirer le filtre ${label}`"
            class="h-7 w-7 rounded-full text-primary-700 hover:bg-primary-400/40 hover:text-primary-900"
            @click="emit('remove')"
        />
    </div>
</template>