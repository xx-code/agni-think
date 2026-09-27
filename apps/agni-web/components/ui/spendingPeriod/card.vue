<script setup lang="ts">
import { SpendingPeriodType, getClassSpendingPeriodType, getLabelSpendingPeriodType, isInProgressSpendingPeriodType } from '~/types/constants/spendingPeriod';
import type { AllSpendingPeriod } from '~/types/ui/spending-period';

const { period } = defineProps<{
    period: AllSpendingPeriod
}>()

const emit = defineEmits<{
    open: [id: string]
    update: [id: string]
    complete: [id: string]
}>()

const expenseRatio = computed(() => {
    return roundNumber(computePercentage(period.totalExpectedIncome, period.totalExpectedExpenses, false))
})

function getBarColor(percent: number): string {
    if (percent >= 100) return 'bg-red-400'
    if (percent >= 75) return 'bg-yellow-400'
    return 'bg-emerald-400'
}

function getStatusColor(percent: number): string {
    if (percent >= 100) return 'text-red-500'
    if (percent >= 75) return 'text-yellow-500'
    return 'text-emerald-500'
}
</script>

<template>
    <UiCard class="group relative flex flex-col gap-4">
        <!-- Card header -->
        <div class="flex items-start justify-between gap-2">
            <div class="flex items-center gap-3 min-w-0">
                <div class="flex items-center justify-center w-10 h-10 shrink-0 rounded-xl bg-gray-50">
                    <UIcon name="i-lucide-calendar-range" class="w-5 h-5 text-gray-600" />
                </div>
                <div class="min-w-0">
                    <h3 class="font-bold text-gray-900 leading-tight truncate">
                        {{ formatDate(period.startDate) }}
                    </h3>
                    <p class="text-xs text-gray-400">au {{ formatDate(period.endDate) }}</p>
                </div>
            </div>

            <!-- State badge -->
            <span
                class="shrink-0 inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold"
                :class="getClassSpendingPeriodType(period.state)"
            >
                {{ getLabelSpendingPeriodType(period.state) }}
            </span>
        </div>

        <!-- Expenses ratio -->
        <div class="space-y-1.5">
            <div class="flex justify-between items-center">
                <span class="text-xs text-gray-500">Dépenses sur revenus</span>
                <span class="text-xs font-medium text-gray-500">{{ expenseRatio }}%</span>
            </div>
            <div class="h-2 w-full bg-gray-100 rounded-full overflow-hidden">
                <div
                    class="h-full rounded-full transition-all duration-500"
                    :class="getBarColor(expenseRatio)"
                    :style="{ width: Math.min(expenseRatio, 100) + '%' }"
                />
            </div>
        </div>

        <!-- Key metrics -->
        <div class="grid grid-cols-3 gap-2 pt-2 border-t border-gray-50">
            <div class="text-center">
                <p class="text-xs text-gray-400 mb-0.5">Revenus</p>
                <p class="text-sm font-semibold text-gray-700">{{ formatCurrency(period.totalExpectedIncome) }}</p>
            </div>
            <div class="text-center border-x border-gray-100">
                <p class="text-xs text-gray-400 mb-0.5">Dépenses</p>
                <p class="text-sm font-bold" :class="getStatusColor(expenseRatio)">
                    {{ formatCurrency(period.totalExpectedExpenses) }}
                </p>
            </div>
            <div class="text-center">
                <p class="text-xs text-gray-400 mb-0.5">Montant libre</p>
                <p class="text-sm font-semibold text-gray-700">{{ formatCurrency(period.freeAmount) }}</p>
            </div>
        </div>

        <!-- Footer -->
        <div class="flex items-center justify-between">
            <div class="flex items-center gap-1.5 text-xs text-gray-400">
                <UIcon name="i-lucide-target" class="w-3.5 h-3.5" />
                <span>{{ period.savingRateTarget }}% d'épargne · {{ period.wantSpendingItems.length }} achats</span>
            </div>
            <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                <UButton
                    v-if="period.state === SpendingPeriodType.Draft"
                    variant="ghost"
                    color="neutral"
                    icon="i-lucide-pencil"
                    size="xs"
                    @click="emit('update', period.id)"
                />
                <UButton
                    v-if="isInProgressSpendingPeriodType(period.state)"
                    variant="ghost"
                    color="success"
                    icon="i-lucide-check"
                    size="xs"
                    @click="emit('complete', period.id)"
                />
                <UButton
                    variant="ghost"
                    color="neutral"
                    icon="i-lucide-eye"
                    size="xs"
                    @click="emit('open', period.id)"
                />
            </div>
        </div>
    </UiCard>
</template>
