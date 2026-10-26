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
            <h4 class="text-lg text-primary font-bold">Reste de la periode</h4>

            <div v-if="period" class="text-[11px]">
                <span>{{ formatDate(period.startDate) }} - {{ formatDate(period.endDate) }}</span>
                <span
                    v-if="daysRemaining !== undefined"
                    class="px-2 py-0.5 rounded-full  font-medium"
                    :class="daysRemaining > 0 ? ' text-gray-600' : 'text-danger'"
                >

                    {{ daysRemaining > 0 ? `${daysRemaining} j restants` : 'Periode terminee' }}
                </span>
            </div>
            
        </div>

        <div v-if="status === 'pending'" class="flex justify-center p-4">
            <UIcon name="i-lucide-loader-circle" class="w-5 h-5 animate-spin text-gray-400" />
        </div>

        <div v-else-if="!period" class="flex items-center gap-2 text-sm text-gray-400 py-2">
            <UIcon name="i-lucide-calendar-range" class="w-4 h-4" />
            Aucune periode en cours
        </div>

        <div v-else class="grid grid-cols-2 gap-4">
            <div class="bg-neutral-100 rounded-3xl p-4">
                <h4 class="text-neutral-400">Reste total</h4>
                <h1 class="text-xl text-primary p-2">{{ formatCurrency(expectedRemain) }}</h1>
            </div>

            <div class="bg-neutral-100 rounded-3xl p-4">
                <h4 class="text-neutral-400">Reste actuel</h4>
                <h1 :class="['text-xl p-2', remainStatus]">{{ formatCurrency(currentRemain) }}</h1>
            </div>
        </div>

        <div v-if="period" class="flex items-center justify-end text-xs text-gray-500 px-2">
            <NuxtLink to="/spending-periods" class="text-neutral-500 hover:underline mt-1">
                Voir la periode
            </NuxtLink>
        </div>
    </div>
</template>
