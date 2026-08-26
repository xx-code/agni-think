<script setup lang="ts">
import type { ProvisionCard } from '~/types/ui/provision';


const { data } = defineProps<{
    data: ProvisionCard
}>()

const emit = defineEmits<{
    update: [id: string]
    delete: [id: string]
}>()

function getBarColor(percent: number): string {
    if (percent >= 100) return 'bg-gray-300'
    if (percent >= 75) return 'bg-red-400'
    if (percent >= 40) return 'bg-yellow-400'
    return 'bg-emerald-400'
}

function getStatusColor(percent: number): string {
    if (percent >= 100) return 'text-gray-400'
    if (percent >= 75) return 'text-red-500'
    if (percent >= 40) return 'text-yellow-500'
    return 'text-emerald-500'
}
// class="group relative flex flex-col gap-4 p-5 bg-white border border-gray-100 rounded-2xl shadow-sm hover:shadow-md transition-all duration-200 hover:-translate-y-0.5"
</script>
<template>
    <UiCard class="group relative flex flex-col gap-4">
        <!-- Card header -->
        <div class="flex items-start justify-between gap-2">
            <div class="flex items-center gap-3 min-w-0">
                <div class="flex items-center justify-center w-10 h-10 shrink-0 rounded-xl bg-gray-50">
                    <UIcon name="i-lucide-box" class="w-5 h-5 text-gray-600" />
                </div>
                <div class="min-w-0">
                    <h3 class="font-bold text-gray-900 leading-tight truncate">{{ data.title }}</h3>
                    <p class="text-xs text-gray-400">Acquis {{ formatDate(data.acquisitionDate) }}</p>
                </div>
            </div>

            <!-- Status badge -->
            <span
                class="shrink-0 inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold"
                :class="{
                    'bg-gray-100 text-gray-500':  data.amortizationPercent >= 100,
                    'bg-red-50 text-red-600': data.amortizationPercent >= 75 && data.amortizationPercent < 100,
                    'bg-yellow-50 text-yellow-600': data.amortizationPercent >= 40 && data.amortizationPercent < 75,
                    'bg-emerald-50 text-emerald-600': data.amortizationPercent < 40,
                }"
            >
                <span v-if="data.amortizationPercent >= 100">Amorti</span>
                <span v-else>{{ data.amortizationPercent }}%</span>
            </span>
        </div>

        <!-- Amortization progress bar -->
        <div class="space-y-1.5">
            <div class="flex justify-between items-center">
                <span class="text-xs text-gray-500">Amortissement</span>
                <span class="text-xs font-medium text-gray-500">
                    <template v-if="data.remainingMonths > 0">
                        {{ data.remainingMonths }} mois restants
                    </template>
                    <template v-else>
                        Durée de vie atteinte
                    </template>
                </span>
            </div>
            <div class="h-2 w-full bg-gray-100 rounded-full overflow-hidden">
                <div
                    class="h-full rounded-full transition-all duration-500"
                    :class="getBarColor(data.amortizationPercent)"
                    :style="{ width: data.amortizationPercent + '%' }"
                />
            </div>
        </div>

        <!-- Key metrics -->
        <div class="grid grid-cols-3 gap-2 pt-2 border-t border-gray-50">
            <div class="text-center">
                <p class="text-xs text-gray-400 mb-0.5">Valeur initiale</p>
                <p class="text-sm font-semibold text-gray-700">{{ formatCurrency(data.initialCost) }}</p>
            </div>
            <div class="text-center border-x border-gray-100">
                <p class="text-xs text-gray-400 mb-0.5">Valeur actuelle</p>
                <p class="text-sm font-bold" :class="getStatusColor(data.amortizationPercent)">
                    {{ formatCurrency(data.residualValue) }}
                </p>
            </div>
            <div class="text-center">
                <p class="text-xs text-gray-400 mb-0.5">Mensualité</p>
                <p class="text-sm font-semibold text-gray-700">{{ formatCurrency(data.monthlyPayment) }}</p>
            </div>
        </div>

        <!-- Footer -->
        <div class="flex items-center justify-between">
            <div class="flex items-center gap-1.5 text-xs text-gray-400">
                <UIcon name="i-lucide-clock" class="w-3.5 h-3.5" />
                <span>{{ data.expectedLifespanMonth }} mois </span>
            </div>
            <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                <UButton
                    variant="ghost"
                    color="neutral"
                    icon="i-lucide-pencil"
                    size="xs"
                    @click="emit('update', data.id)"
                />
                <UButton
                    variant="ghost"
                    color="error"
                    icon="i-lucide-trash-2"
                    size="xs"
                    @click="emit('delete', data.id)"
                />
            </div>
        </div>
    </UiCard>
</template>