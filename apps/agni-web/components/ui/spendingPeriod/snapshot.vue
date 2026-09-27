<script setup lang="ts">
import type { SpendingPeriodSnapshot } from '~/types/ui/spending-period';

const { snapshot } = defineProps<{
    snapshot: SpendingPeriodSnapshot
}>()

const lines = computed(() => {
    return [
        { title: 'Revenus', amount: snapshot.income, icon: 'i-lucide-trending-up' },
        { title: 'Depenses fixes', amount: snapshot.fixExpenses, icon: 'i-lucide-receipt' },
        { title: 'Depenses variables', amount: snapshot.variableExpenses, icon: 'i-lucide-repeat' },
        { title: 'Budgets', amount: snapshot.budgetExpenses, icon: 'i-lucide-wallet' }
    ]
})

const totalExpenses = computed(() => {
    return snapshot.fixExpenses + snapshot.variableExpenses + snapshot.budgetExpenses
})

const balance = computed(() => {
    return snapshot.income - totalExpenses.value
})
</script>

<template>
    <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 space-y-4">
        <div class="flex items-center gap-2">
            <UIcon name="i-lucide-camera" class="w-4 h-4 text-gray-500" />
            <h2 class="font-semibold text-gray-900">Snapshot de la periode</h2>
        </div>

        <div class="grid grid-cols-2 gap-3">
            <div class="rounded-xl bg-emerald-50 p-3">
                <p class="text-xs text-emerald-600 mb-0.5">Revenus</p>
                <p class="text-sm font-semibold text-emerald-700">{{ formatCurrency(snapshot.income) }}</p>
            </div>
            <div class="rounded-xl bg-red-50 p-3">
                <p class="text-xs text-red-600 mb-0.5">Depenses</p>
                <p class="text-sm font-semibold text-red-700">{{ formatCurrency(totalExpenses) }}</p>
            </div>
            <div class="rounded-xl bg-blue-50 p-3">
                <p class="text-xs text-blue-600 mb-0.5">Epargne</p>
                <p class="text-sm font-semibold text-blue-700">{{ formatCurrency(snapshot.saving) }}</p>
            </div>
            <div class="rounded-xl bg-gray-50 p-3">
                <p class="text-xs text-gray-400 mb-0.5">Solde</p>
                <p class="text-sm font-semibold text-gray-800">{{ formatCurrency(balance) }}</p>
            </div>
        </div>

        <div class="space-y-2">
            <div
                v-for="line in lines"
                :key="line.title"
                class="flex items-center justify-between text-sm border-b border-gray-50 last:border-b-0 py-1.5">
                <span class="flex items-center gap-2 text-gray-700">
                    <UIcon :name="line.icon" class="w-3.5 h-3.5 text-gray-400" />
                    {{ line.title }}
                </span>
                <span class="font-medium text-gray-800">{{ formatCurrency(line.amount) }}</span>
            </div>
        </div>
    </div>
</template>
