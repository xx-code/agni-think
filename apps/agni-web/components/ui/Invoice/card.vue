<script lang="ts" setup>
import type { DropdownMenuItem } from '@nuxt/ui';
import type { DeductionType } from '~/types/ui/deduction';
import type { InvoiceType } from '~/types/ui/transaction';

const { data, deductions } = defineProps<{
    data: InvoiceType
    deductions: DeductionType[]
}>()

const emit = defineEmits<{
    update: [invoiceId: string]
    delete: [invoiceId: string]
    cancelTransfert: [invoiceId: string]
    valide: [invoiceId: string]
}>()

const isExpanded = ref(false)

const formatActionItems = () => {
    const editActions = 
        {
            class: 'items-center',
            label: 'Modifier',
            icon: 'i-lucide-square-pen',
            onSelect: () => emit('update', data.id)
        }
    const completeAction =
        {
            class: 'items-center',
            label: 'Valider',
            icon: 'i-lucide-check',
            onSelect: () => emit('valide', data.id)
        }

    const cancelActions= {
            class: 'items-center',
            label: 'Annuler transfert',
            icon: 'i-lucide-trash',
            color: 'error',
            onSelect: () => emit('cancelTransfert', data.id)
    }
    const deletAction = {
            class: 'items-center',
            label: 'Supprimer',
            icon: 'i-lucide-trash',
            color: 'error',
            onSelect: () => emit('delete', data.id)
        }
    
    const listEdit = [editActions]
    const listRemove = [deletAction]

    if (data.isFreeze) {
        listEdit.splice(0, 1)
    }

    if (data.status.toLowerCase() === 'pending') {
        listEdit.push(completeAction)
    }

    return [
        listEdit,
        listRemove
    ]
} 

const actionItems = ref<DropdownMenuItem[][]>(formatActionItems()) 

const MOVEMENT_COLORS: Record<string, string> = {
    income: '#10b981',
    other: '#b2bac4',
}
const DEFAULT_MOVEMENT_COLOR = '#ef4444'

function getMovementColor(type?: string) {
    return MOVEMENT_COLORS[type?.toLowerCase() ?? ''] ?? DEFAULT_MOVEMENT_COLOR
}

function getRecordTypeColor(type?: string) {
    return type?.toLowerCase() === 'credit' ? '#10b981' : '#ef4444'
}

function withAlpha(hex: string, alpha = '22') {
    return `${hex}${alpha}`
}

const invoiceDescription = computed(() => {
    if (data.transactions.length === 0) return '---'
    if (data.transactions.length === 1) return data.transactions[0]?.description
    return `${data.transactions.length} articles`
})


const invoiceIcons = computed(() => {
    if (data.transactions.length === 0)
        return [{ name: 'i-lucide-file-question-mark', color: '#b2bac4' }]

    const seen = new Set<string>()
    const icons: { name: string, color: string }[] = []

    for (const t of data.transactions) {
        const key = `${t.category.icon}__${t.category.color}`
        if (!seen.has(key)) {
            seen.add(key)
            icons.push({ name: t.category.icon, color: t.category.color })
        }
    }

    if (icons.length > 3)
        return [...icons.slice(0, 2), { name: 'i-lucide-plus', color: '#b2bac4' }]

    return icons
})

const invoiceColor = computed(() => getMovementColor(data.mouvement))
</script>

<template>
    <UiListCard>
        <div
            class="group flex items-center gap-3 p-4 cursor-pointer select-none hover:bg-neutral-50 dark:hover:bg-neutral-800/50 transition-colors"
            @click="isExpanded = !isExpanded"
        >
            <UButton
                :icon="isExpanded ? 'i-lucide-chevron-up' : 'i-lucide-chevron-down'"
                variant="ghost"
                color="neutral"
                size="sm"
                @click.stop="() => {isExpanded = !isExpanded}"
            />

            <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2">
                    <p class="font-medium truncate">{{ formatDate(data.date) }}</p>
                    <UKbd 
                        v-if="data.status.toLowerCase() === 'pending'"
                        size="sm"
                        color="warning"
                    >En attend</UKbd>
                </div>
                <p class="text-sm font-medium text-neutral-400 truncate">{{ invoiceDescription }}</p>
            </div>

            <div class="flex items-center gap-4 shrink-0">
                <div class="flex ">
                    <div
                        v-for="(icon, idx) in invoiceIcons"
                        :key="icon.name + idx"
                        class="flex items-center justify-center rounded-full ring-2 ring-white dark:ring-neutral-900"
                        :class="{ '-ml-2': idx > 0 }"
                        :style="{ background: withAlpha(icon.color), width: '26px', height: '26px' }"
                    >
                        <UIcon :name="icon.name" class="text-sm" :style="{ color: icon.color }" />
                    </div>
                </div>

                <p class="font-semibold tabular-nums" :style="{ color: invoiceColor }">
                    {{ formatCurrency(data.total) }}
                </p>

                <UDropdownMenu 
                    :items="actionItems"
                >
                    <UButton 
                        icon="i-lucide-ellipsis-vertical"
                        variant="ghost"
                        color="neutral"
                        @click.stop
                    />
                </UDropdownMenu>
                
            </div>
        </div>

        <!-- Détail : partage le même conteneur que le header, séparé par une simple bordure -->
        <div v-if="isExpanded" class="border-t border-neutral-200 dark:border-neutral-800 px-4 pb-4 pt-3 space-y-3">
            <div
                v-for="trans in data.transactions"
                :key="trans.id"
                class="flex items-center gap-3"
            >
                <div
                    class="flex items-center justify-center rounded-full shrink-0"
                    :style="{ background: withAlpha(trans.category.color), width: '40px', height: '40px' }"
                >
                    <UIcon :name="trans.category.icon" class="text-lg" :style="{ color: trans.category.color }" />
                </div>

                <div class="flex-1 min-w-0">
                    <p class="font-medium truncate">{{ trans.category.title }}</p>
                    <p class="text-sm font-medium text-neutral-400 truncate">{{ trans.description }}</p>

                    <!--Budget et tags-->
                    <div class="flex items-center flex-wrap">
                        <UKbd 
                            v-for="budget in trans.budgets"
                            :key="budget.id"
                            :value="budget.value"
                            size="sm"
                        />
                        <UKbd 
                            v-for="tag in trans.tags"
                            :key="tag.id"
                            :value="tag.value"
                            size="sm"
                            :style="{
                                borderColor: tag.color,
                                color: tag.color
                            }"
                        />
                    </div>
                </div>

                <p class="font-medium tabular-nums shrink-0" :style="{ color: getRecordTypeColor(data.mouvement) }">
                    {{ formatCurrency(trans.amount) }}
                </p>
            </div>

            <div v-if="data.deductions.length > 0" class="pt-3 border-t border-neutral-200 dark:border-neutral-800">
                <p class="text-sm font-semibold text-neutral-500 dark:text-neutral-400 mb-2">Déductions</p>
                <div class="space-y-1.5">
                    <div
                        v-for="deduction in data.deductions"
                        :key="deduction.id"
                        class="flex justify-between items-center px-2.5 py-1.5 bg-amber-50 dark:bg-amber-500/10 rounded-lg"
                    >
                        <span class="text-sm font-medium text-neutral-600 dark:text-neutral-300">
                            {{ deductions.find(i => deduction.id === i.id)?.description ?? 'Inconnue' }}
                        </span>
                        <span class="text-sm font-semibold text-amber-700 dark:text-amber-400">
                            -{{ formatCurrency(deduction.amount) }}
                        </span>
                    </div>
                </div> 
            </div>

            <div :class="[
                'pt-3 border-t border-neutral-200 dark:border-neutral-800 flex',
                data.status.toLowerCase() === 'pending' ? 'justify-between' : 'justify-end'
            ]">
                <div v-if="data.status.toLowerCase() === 'pending'" class="flex items-end gap-3">
                    <UButton 
                        label="Annuler"
                        variant="outline" 
                        color="neutral"
                        size="sm"
                        @click="emit('delete', data.id)"
                    />
                    <UButton 
                        label="Confirmer"
                        color="success"
                        size="sm"
                        @click="emit('valide', data.id)"
                    />
                </div>

                <div class="space-y-1 min-w-[200px]">
                    <div class="flex justify-between text-sm text-neutral-500 dark:text-neutral-400">
                        <span>Sous-total</span>
                        <span class="font-medium text-neutral-700 dark:text-neutral-200">{{ formatCurrency(data.subTotal) }}</span>
                    </div>
                    <div v-if="data.deductions.length > 0" class="flex justify-between text-sm">
                        <span class="text-neutral-500 dark:text-neutral-400">Déductions</span>
                        <span class="font-medium text-amber-600">
                            -{{ formatCurrency(data.deductions.reduce((s, d) => s + d.amount, 0)) }}
                        </span>
                    </div>
                    <div class="flex justify-between text-base font-semibold pt-1.5 border-t border-neutral-200 dark:border-neutral-800">
                        <span>Total</span>
                        <span :style="{ color: invoiceColor }">{{ formatCurrency(data.total) }}</span>
                    </div>
                </div>
            </div>
        </div>
    </UiListCard>
</template>