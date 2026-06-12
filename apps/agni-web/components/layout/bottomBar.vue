<script lang="ts" setup>
    const message = ref({ message: 'Message au comptable'})
</script>

<template>
    <div class="p-1.5 flex justify-end">
        <UPopover :dismissible="false" :content="{ side: 'top', align: 'center' }">
            <UButton 
                size="sm" 
                variant="ghost"
                leading-icon="i-lucide-calculator">
                Demander
            </UButton>

            <template #content>
                <FormAccountingChat v-model="message" />
            </template>
        </UPopover> 
    </div>
</template>