<script setup lang="ts">
const props = defineProps<{
    icon?: string
    title?: string
    placeholder?: string
    items: {value: string, label:string}[]
}>()

const model = defineModel<string[]>()
</script>

<template>
    <USelectMenu 
        v-if="model"
        :ui="{
            base: model.length > 0  ? 
                'border border-primary text-primary bg-primary/10' 
                : ''
        }"
        multiple
        clear
        class="min-w-40"
        :placeholder="placeholder"
        variant="outline"
        color="neutral"
        size="lg"
        value-key="value"
        :items="items"
        v-model="model"
    >
        <template v-if="title" #default>
            <div class="flex items-center gap-2">
                <UIcon 
                    v-if="icon"
                    :class="['text-muted', model.length > 0 ? 'text-primary' : '']"
                    :name="icon" />
                <div class="text-sm font-medium">
                    <span >
                        {{ title }}
                    </span>
                    <span v-if="(model.length) > 0">
                        : {{ model.length }}
                    </span>
                </div>
                
            </div>
        </template>

        <template #item="{ item }">
            <div class="flex items-center gap-2">
                <UCheckbox :model-value="model?.includes(item.value)" readonly />
                <span>{{ item.label }}</span>
            </div>
        </template>
    </USelectMenu>
</template>