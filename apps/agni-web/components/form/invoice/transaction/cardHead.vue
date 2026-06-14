<script lang="ts" setup>
import type { CategoryType } from '~/types/ui/category';

const { order, categories, hasMoreTransactions } = defineProps<{
    order: number,
    categories: CategoryType[],
    hasMoreTransactions: boolean,
}>()

const model = defineModel<{
    categoryId: string
}>()

const emit = defineEmits<{
    remove: []
    duplicate: []
}>()

function getCategoryById(id: string) {
    return categories.find(c => c.id === id);
}

</script>

<template>
    <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
            <!-- Icône de catégorie -->
            <div 
                v-if="model?.categoryId"
                class="flex items-center justify-center rounded-full"
                :style="{
                    background: `${getCategoryById(model?.categoryId)?.color}22`,
                    width: '40px',
                    height: '40px',
                }"
            >
                <UIcon 
                    :name="getCategoryById(model?.categoryId)?.icon || 'i-lucide-circle'" 
                    class="text-lg"
                    :style="{ color: getCategoryById(model?.categoryId)?.color }" 
                />
            </div>
            <span class="font-medium text-gray-700 dark:text-gray-300">
                Article {{ order }}
            </span>
        </div>

        <div class="flex items-center gap-2">
            <UButton 
                icon="i-lucide-copy"
                color="neutral"
                variant="ghost"
                size="sm"
                class="rounded-full"
                @click="emit('duplicate')"
            />
            <UButton 
                v-if="hasMoreTransactions"
                icon="i-lucide-trash-2"
                color="error"
                variant="ghost"
                size="sm"
                class="rounded-full"
                @click="emit('remove')"
            />
        </div>
    </div>
</template>