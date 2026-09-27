<script setup lang="ts">
import type { ForcastSpendingAchieveItem, ForcastSpendingPeriod } from '~/types/ui/spending-period';

const { forcast } = defineProps<{
    forcast: ForcastSpendingPeriod
}>()

const metrics = computed(() => {
    return [
        {
            title: 'Revenus',
            icon: 'i-lucide-trending-up',
            current: forcast.currentIncome,
            expected: forcast.totalExpectedIncome
        },
        {
            title: 'Depenses',
            icon: 'i-lucide-trending-down',
            current: forcast.currentBudgetExpense,
            expected: forcast.totalExpectedExpense
        },
        {
            title: 'Epargne',
            icon: 'i-lucide-piggy-bank',
            current: forcast.currentSaving,
            expected: forcast.expectedSaving
        },
        {
            title: 'Reste a ALLouer',
            icon: 'i-lucide-wallet',
            current: forcast.currentRemainAmount,
            expected: forcast.expectedRemainAmount
        }
    ]
})

const expectedExpenses = computed(() => {
    return [
        { title: 'Fixes', amount: forcast.expectedFixExpense },
        { title: 'Variables', amount: forcast.expectedVariableExpense },
        { title: 'Budgets', amount: forcast.expectedBudgetExpense }
    ]
})

const itemSections = computed(() => {
    return [
        { title: 'Revenus saisis', icon: 'i-lucide-banknote', items: forcast.incomeItems },
        { title: 'Depenses fixes saisies', icon: 'i-lucide-receipt', items: forcast.fixExpenseItems },
        { title: 'Depenses variables saisies', icon: 'i-lucide-repeat', items: forcast.variableExpenseItems },
        { title: 'Achats souhaites atteints', icon: 'i-lucide-gift', items: forcast.achievedWishedItems }
    ]
})

function isAchieved(item: ForcastSpendingAchieveItem): boolean {
    return item.isAchieved
}
</script>

<template>
    <div class="space-y-4">
        <!-- Forecast summary -->
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 space-y-4">
            <div class="flex items-center gap-2">
                <UIcon name="i-lucide-gauge" class="w-4 h-4 text-gray-500" />
                <h2 class="font-semibold text-gray-900">Prevision vs reel</h2>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div v-for="metric in metrics" :key="metric.title" class="space-y-2">
                    <div class="flex justify-between items-center">
                        <span class="flex items-center gap-1.5 text-xs text-gray-500">
                            <UIcon :name="metric.icon" class="w-3.5 h-3.5" />
                            {{ metric.title }}
                        </span>
                        <span class="text-xs text-gray-400">
                            {{ roundNumber(computePercentage(metric.expected, metric.current)) }}%
                        </span>
                    </div>
                    <div class="text-sm">
                        <span class="font-semibold text-gray-900">{{ formatCurrency(metric.current) }}</span>
                        <span class="text-gray-400 mx-1">/</span>
                        <span class="text-gray-500">{{ formatCurrency(metric.expected) }}</span>
                    </div>
                    <UProgress
                        :ui="{ base: 'bg-gray-50', indicator: '' }"
                        :model-value="roundNumber(computePercentage(metric.expected, metric.current))"
                        size="sm"
                    />
                </div>
            </div>
        </div>

        <!-- Expected expenses breakdown -->
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 space-y-4">
            <h2 class="font-semibold text-gray-900">Detail des depenses attendues</h2>
            <div class="grid grid-cols-3 gap-3">
                <div v-for="expense in expectedExpenses" :key="expense.title" class="rounded-xl bg-gray-50 p-3 text-center">
                    <p class="text-xs text-gray-400 mb-0.5">{{ expense.title }}</p>
                    <p class="text-sm font-semibold text-gray-800">{{ formatCurrency(expense.amount) }}</p>
                </div>
            </div>
        </div>

        <!-- Items -->
        <div
            v-for="section in itemSections"
            :key="section.title"
            v-show="section.items.length > 0"
            class="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 space-y-3">
            <div class="flex items-center gap-2">
                <UIcon :name="section.icon" class="w-4 h-4 text-gray-500" />
                <h2 class="font-semibold text-gray-900">{{ section.title }}</h2>
                <span class="text-xs text-gray-400 ml-auto">{{ section.items.length }}</span>
            </div>

            <div
                v-for="(item, index) in section.items"
                :key="index"
                class="flex items-center justify-between text-sm border-b border-gray-50 last:border-b-0 py-1.5">
                <div class="flex items-center gap-2 min-w-0">
                    <UIcon
                        :name="isAchieved(item) ? 'i-lucide-circle-check' : 'i-lucide-circle'"
                        :class="[
                            'w-4 h-4 shrink-0',
                            isAchieved(item) ? 'text-emerald-500' : 'text-gray-300'
                        ]"
                    />
                    <span class="text-gray-700 truncate">{{ item.description }}</span>
                </div>
                <div class="flex items-center gap-2 shrink-0">
                    <span class="font-medium text-gray-800">{{ formatCurrency(item.validAmount) }}</span>
                    <span
                        :class="['text-xs text-gray-400', item.validAmount >= item.amount ? 'line-through' : '']"
                    >
                        {{ formatCurrency(item.amount) }}
                    </span>
                </div>
            </div>
        </div>
    </div>
</template>
