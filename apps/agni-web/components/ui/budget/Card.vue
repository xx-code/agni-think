<script setup lang="ts">
import type { DropdownMenuItem } from '@nuxt/ui';
import type { BudgetCard } from '~/types/ui/budget';

const { budget } = defineProps<{
    budget: BudgetCard
}>()

const emit = defineEmits<{
    update: [id: string]
    delete: [id: string]
    archive: [id: string]
}>()

const percEv = computed(() => roundNumber(computePercentage(budget.target, budget.balance, false))) 

const isClosed = computed(() => {
    if (budget.dueDate.getTime() < (new Date()).getTime() && (budget.repeater === undefined || budget.repeater === null))
        return true

    return false
})

const budgetColorMap = computed(() => {
    if (budget.balance > budget.target)
        return { textColor: 'text-red-600', progressColor: 'bg-red-500', bgColor: 'rgb(237, 28, 36, 0.1)', title: 'depasse'}
    else if (percEv.value === 100)
        return { textColor: 'text-amber-600', progressColor: 'bg-amber-500', bgColor: 'rgb(255, 185, 0, 0.1)', title: 'atteint'}

    return { textColor: 'text-success-600', progressColor: 'bg-success-500', bgColor: 'rgb(88, 196, 42, 0.1)', title: ''}
})

const actionItems = ref<DropdownMenuItem[][]>([
    [
        {
            class: 'items-center',
            label: 'Modifier',
            icon: 'i-lucide-square-pen',
            onSelect: () => emit('update', budget.id)
        },
    ],
    [
        {
            class: 'items-center',
            label: 'Archiver',
            icon: 'i-lucide-archive',
            color: 'warning',
            onSelect: () => emit('archive', budget.id)
        },
        {
            class: 'items-center',
            label: 'Supprimer',
            icon: 'i-lucide-trash',
            color: 'error',
            onSelect: () => emit('delete', budget.id)
        }
    ]
])
</script>

<template>
    <div :class="[
            'p-5 rounded-xl bg-white border  h-full cursor-pointer hover:shadow-sm',
            percEv > 100 ? 'border-red-200' : 'border-gray-200'
        ]">
        <div class="flex items-start gap-1 mb-4">
            <h3 class="flex-1 truncate min-w-0 font-bold text-lg" :title="budget.title">{{ budget.title }}</h3>
            <span
                :class="`px-2.5 py-1 font-medium text-[0.70rem] ${budgetColorMap.textColor} rounded-full`" 
                :style="{backgroundColor: budgetColorMap.bgColor}">{{ percEv }}% {{ budgetColorMap.title }}</span>
            <UDropdownMenu :items="actionItems" >
                <UButton 
                    @click.stop
                    icon="i-lucide-ellipsis-vertical"
                    variant="ghost"
                    color="neutral"
                />
            </UDropdownMenu> 
        </div>

        <div>
            <h6 class="text-xs font-semibold text-gray-500">Restant</h6>
            <UiCardProgress 
                class="mb-2"
                :balance="budget.balance"
                :target-amount="budget.target"
                :balance-color="budgetColorMap.textColor"
                :progress-color="budgetColorMap.progressColor"
                :show-percentage="false"
                :show-balance-remain="true"
            />
            <div class="flex items-center justify-between font-semibold text-sm">
                <div class="flex items-center text-gray-500">
                    <UIcon name="i-lucide-credit-card" />
                    <span class="ml-1">Depense</span>
                </div>
                <h6>{{ formatCurrency(budget.balance)  }}</h6>
            </div>
        </div>

        <USeparator class="w-full my-4"/>

        <div class="flex items-center justify-between text-sm font-semibold">
            <div class="flex items-center">
                <UIcon :name="budget.repeater ? 'i-lucide-refresh-cw' : 'i-lucide-calendar-range'" />
                <span class="ml-1">{{ budget.repeater ? 'Reprend le ' : 'Termine le ' }}</span>
                <span class="ml-1">{{ formatDate(budget.dueDate) }}<span class="text-gray-400" v-if="isClosed">
                   {{ (budget.dueDate.getTime() < (new Date()).getTime()) ? ' (deja passe)' : ` (dans ${getDaysRemaining(budget.dueDate)} jours)`}}
                </span> </span>
            </div>
            
            <UKbd v-if="isClosed">Cloturer</UKbd>
        </div>

    </div>
</template>