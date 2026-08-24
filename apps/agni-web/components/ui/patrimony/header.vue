<script setup lang="ts">
const { networth, monthlyEvolution, totalLiability, totalAsset } = defineProps<{
    networth: number
    monthlyEvolution: number
    totalLiability: number
    totalAsset: number
}>()

function computeEvolution(totalAmount: number, amount: number) {
    if (totalAmount === 0) return 0

    return (amount/totalAmount) * 100
}

const percentageAsset = computed(() => computeEvolution(totalAsset + Math.abs(totalLiability), totalAsset)) 
const percentageLiability = computed(() => computeEvolution(totalAsset + Math.abs(totalLiability), totalLiability)) 

</script>

<template>
    <div>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 ">
            <UiCard>
                <h4 class="text-md font-semibold text-gray-400 mb-2">Valeur nette totale</h4>
                <p class="font-semibold text-2xl text-gray-800">
                    {{ formatCurrency(networth) }}
                </p>
            </UiCard>

            <UiCard>
                <h4 class="text-md font-semibold text-gray-400 mb-2">Évolution mensuelle</h4>
                <p :class="[
                    'font-semibold text-2xl flex items-center gap-1',
                    monthlyEvolution > 0 ? 'text-green-500' : 'text-red-500']">
                    {{ roundNumber(monthlyEvolution) }}%
                </p>
            </UiCard>

            <UiCard>
                <h4 class="text-md font-semibold text-gray-400 mb-2">Actifs vs Passifs</h4>
                <div class="flex items-center justify-between font-semibold text-2xl mb-2">
                    <h3 class="text-green-500">{{ roundNumber(percentageAsset) }}%</h3>
                    <h3 class="text-red-500">{{ roundNumber(percentageLiability) }}%</h3>
                </div>
                <div class="w-full flex">
                    <div class="bg-green-700 p-1 rounded-l-full" :style="{width: `${percentageAsset}%`}" />
                    <div class="bg-red-700 p-1 rounded-r-full" :style="{width: `${percentageLiability}%`}" />
                </div>
            </UiCard>
        </div>
    </div>
</template>   