<script setup lang="ts">
import type { DropdownMenuItem } from '@nuxt/ui';
import type { CategoryType } from '~/types/ui/category';
import type { ScheduleInvoiceType } from '~/types/ui/scheduleTransaction';

const props = defineProps<{
    data: ScheduleInvoiceType
    categories: CategoryType[]
}>()

const emit = defineEmits<{
    update: [invoiceId: string]
    delete: [invoiceId: string]
    togglePause: [invoiceId: string]
}>()

function withAlpha(hex: string, alpha = '22') {
    return `${hex}${alpha}`
}

const category = computed(() => {
    const categoryFound = props.categories.find(i => i.id === props.data.categoryId)
    if (!categoryFound)
        return { label: 'Inconnue', color: '#b2bac4', icon: 'i-lucide-circle-question-mark' }

    return { label: categoryFound.title, color: categoryFound.color, icon: categoryFound.icon }
})

const dueStatus = computed(() => {
    const diffDays = Math.ceil((new Date(props.data.dueDate).getTime() - Date.now()) / (1000 * 60 * 60 * 24))
    if (diffDays < 0) return 'overdue'
    if (diffDays <= 3) return 'soon'
    return 'normal'
})

const dueDateClass = computed(() => ({
    'text-red-500': dueStatus.value === 'overdue',
    'text-amber-500': dueStatus.value === 'soon',
}))

const actionItems = computed<DropdownMenuItem[][]>(() => [
    [
        {
            label: 'Modifier',
            icon: 'i-lucide-square-pen',
            onSelect: () => emit('update', props.data.id)
        },
    ],
    [
        {
            label: 'Supprimer',
            icon: 'i-lucide-trash',
            color: 'error',
            onSelect: () => emit('delete', props.data.id)
        }
    ]
])
</script>

<template>
    <UiListCard
        class="group flex items-center gap-3 p-4 cursor-pointer select-none hover:bg-neutral-50 dark:hover:bg-neutral-800/50 transition-colors"
        @click="emit('update', data.id)"
    >
        <div
            class="flex items-center justify-center rounded-full shrink-0"
            :style="{ background: withAlpha(category.color), width: '40px', height: '40px' }"
        >
            <UIcon :name="category.icon" class="text-lg" :style="{ color: category.color }" />
        </div>

        <div class="flex-1 min-w-0">
            <p class="font-medium truncate flex items-center gap-1.5" :class="dueDateClass">
                {{ formatDate(data.dueDate) }}
                <span v-if="dueStatus === 'overdue'" class="text-xs font-medium px-1.5 py-0.5 rounded bg-red-100 dark:bg-red-500/10">
                    En retard
                </span>
                <span v-else-if="dueStatus === 'soon'" class="text-xs font-medium px-1.5 py-0.5 rounded bg-amber-100 dark:bg-amber-500/10">
                    Bientôt
                </span>
            </p>
            <p class="text-sm font-medium text-neutral-400 truncate">{{ category.label }} - {{ data.name }}</p>
        </div>

        <UButton
            variant="ghost"
            :icon="data.isPause ? 'i-lucide-play-circle' : 'i-lucide-pause-circle'"
            :class="data.isPause ? 'text-emerald-500' : 'text-amber-500'"
            :aria-label="data.isPause ? 'Reprendre' : 'Mettre en pause'"
            @click.stop="emit('togglePause', data.id)"
        />

        <p
            class="font-semibold tabular-nums"
            :class="data.type.toLowerCase() === 'income' ? 'text-emerald-500' : 'text-red-500'"
        >
            {{ formatCurrency(data.amount) }}
        </p>

        <UDropdownMenu :items="actionItems">
            <UButton
                icon="i-lucide-ellipsis-vertical"
                variant="ghost"
                color="neutral"
                aria-label="Plus d'actions"
                @click.stop
            />
        </UDropdownMenu>
    </UiListCard>
</template>