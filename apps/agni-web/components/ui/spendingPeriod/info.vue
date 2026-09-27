<script setup lang="ts">
import type { SpendingPeriod } from '~/types/ui/spending-period';

const { period } = defineProps<{
    period: SpendingPeriod
}>()
</script>

<template>
    <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 space-y-4">
        <div class="flex items-center gap-2">
            <UIcon name="i-lucide-calendar-range" class="w-4 h-4 text-gray-500" />
            <h2 class="font-semibold text-gray-900">Periode</h2>
            <span class="text-sm text-gray-500 ml-auto">
                {{ formatDate(period.startDate) }} - {{ formatDate(period.endDate) }}
            </span>
        </div>

        <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
            <div class="rounded-xl bg-gray-50 p-3">
                <p class="text-xs text-gray-400 mb-0.5">Revenus</p>
                <p class="text-sm font-semibold text-gray-800">{{ formatCurrency(period.totalExpectedIncome) }}</p>
            </div>
            <div class="rounded-xl bg-gray-50 p-3">
                <p class="text-xs text-gray-400 mb-0.5">Depenses</p>
                <p class="text-sm font-semibold text-gray-800">{{ formatCurrency(period.totalExpectedExpenses) }}</p>
            </div>
            <div class="rounded-xl bg-gray-50 p-3">
                <p class="text-xs text-gray-400 mb-0.5">Montant libre</p>
                <p class="text-sm font-semibold text-gray-800">{{ formatCurrency(period.freeAmount) }}</p>
            </div>
            <div class="rounded-xl bg-gray-50 p-3">
                <p class="text-xs text-gray-400 mb-0.5">Epargne visee</p>
                <p class="text-sm font-semibold text-gray-800">{{ period.savingRateTarget }}%</p>
            </div>
        </div>

        <div v-if="period.wantSpendingItems.length > 0" class="space-y-2">
            <h3 class="text-sm font-medium text-gray-500">Achats souhaites</h3>
            <div
                v-for="(item, index) in period.wantSpendingItems"
                :key="index"
                class="flex items-center justify-between text-sm border-b border-gray-50 last:border-b-0 py-1.5">
                <span class="text-gray-700">{{ item.description }}</span>
                <span class="font-medium text-gray-800">{{ formatCurrency(item.amount) }}</span>
            </div>
        </div>
    </div>
</template>
