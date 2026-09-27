<script setup lang="ts">
import { useInProgressSpendingPeriod } from '~/composables/spendingPeriod'

const { data: period, status } = useInProgressSpendingPeriod()

const daysRemaining = computed(() => {
    if (!period.value)
        return undefined

    return getDaysRemaining(period.value.endDate)
})

const expectedRemain = computed(() => period.value?.forcast?.expectedRemainAmount ?? 0)
const currentRemain = computed(() => period.value?.forcast?.currentRemainAmount ?? 0)

const remainStatus = computed(() => {
    if (expectedRemain.value <= 0)
        return 'text-gray-600'

    return currentRemain.value >= expectedRemain.value ? 'text-green-600' : 'text-red-600'
})
</script>

<template>
    <div class="space-y-1">
        <div class="flex items-center justify-between">
            <h4 class="text-gray-500 font-semibold">Reste de la periode</h4>

            <span
                v-if="daysRemaining !== undefined"
                class="px-2 py-0.5 rounded-full text-[11px] font-medium"
                :class="daysRemaining > 0 ? 'bg-gray-100 text-gray-600' : 'bg-red-100 text-red-700'"
            >
                {{ daysRemaining > 0 ? `${daysRemaining} j restants` : 'Periode terminee' }}
            </span>
        </div>

        <div v-if="status === 'pending'" class="flex justify-center p-4">
            <UIcon name="i-lucide-loader-circle" class="w-5 h-5 animate-spin text-gray-400" />
        </div>

        <div v-else-if="!period" class="flex items-center gap-2 text-sm text-gray-400 py-2">
            <UIcon name="i-lucide-calendar-range" class="w-4 h-4" />
            Aucune periode en cours
        </div>

        <div v-else class="grid grid-cols-2">
            <div>
                <h4 class="text-gray-500 font-semibold">Reste total</h4>
                <h1 class="font-semibold text-2xl p-2">{{ formatCurrency(expectedRemain) }}</h1>
            </div>

            <div>
                <h4 class="text-gray-500 font-semibold">Reste actuel</h4>
                <h1 :class="['font-semibold text-2xl p-2', remainStatus]">{{ formatCurrency(currentRemain) }}</h1>
            </div>
        </div>

        <div v-if="period" class="flex items-center justify-between text-xs text-gray-500 px-2">
            <span>{{ formatDate(period.startDate) }} - {{ formatDate(period.endDate) }}</span>
            <NuxtLink to="/spending-periods" class="font-semibold text-primary-500 hover:underline">
                Voir la periode
            </NuxtLink>
        </div>
    </div>
</template>
