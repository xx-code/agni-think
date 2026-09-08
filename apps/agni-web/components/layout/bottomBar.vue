<script lang="ts" setup>
const open = ref(false)
</script>

<template>
    <div class="flex justify-end p-1.5">
        <UPopover :open="open" :dismissible="false" :content="{ side: 'top', align: 'end' }">
            <UButton
                size="sm"
                variant="ghost"
                leading-icon="i-lucide-calculator"
                @click="() => { open = !open }"
            >
                Demander
            </UButton>
            <template #content>
                <div class="relative">
                    <UButton
                        variant="ghost"
                        color="neutral"
                        size="xs"
                        icon="i-lucide-x"
                        class="absolute right-1 top-1 z-10"
                        aria-label="Fermer"
                        @click="() => { open = false }"
                    />
                    <FormAccountingChat />
                </div>
            </template>
        </UPopover>
    </div>
</template>