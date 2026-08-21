<script setup lang="ts">
import type { TypePatrimony } from '~/types/constants/patrimony';

const { balance, title, evolution, type } = defineProps<{
    balance: number
    title: string
    evolution: number
    type: TypePatrimony
}>()

const getIndicator = computed(() => {
    const ev = roundNumber(evolution)
    if (ev === 0)
        return { textColor: 'text-gray-400', icon: 'i-lucide-minus' }

    let isUp = ev > 0
    if (type === 'Liability')
        isUp = ev < 0
    
    return isUp ? { textColor: 'text-green-500', icon: 'i-lucide-trending-up' } : { textColor: 'text-red-500', icon: 'i-lucide-trending-down'}
})



</script>

<template>
    <div>
        <h1 class="text-2xl font-semibold mb-2">{{ title }}</h1>
        <div class="flex justify-between items-end">
            <span class="text-3xl font-bold">{{ formatCurrency(balance) }}</span>
            <div class="flex items-center text-sm font-bold" :class="getIndicator.textColor">
                <UIcon class="mr-1" :name="getIndicator.icon" />
                <span>
                    {{ roundNumber(evolution)  }}
                </span>
                <span>%</span>
            </div>
        </div>
    </div>
</template>