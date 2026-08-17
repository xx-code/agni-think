<script setup lang="ts"">

const { 
    targetAmount, 
    balance, 
    balanceColor,
    progressColor,
    showPercentage=false, 
    showBalanceRemain=false } = defineProps<{
    targetAmount: number
    balance: number
    balanceColor?: string
    progressColor?: string
    showPercentage?: boolean
    showBalanceRemain?: boolean
}>()

const percEv = computed(() => {
    return roundNumber(computePercentage(targetAmount, balance))
}) 
</script>

<template>
    <div>
        <div class="mb-2 flex justify-between items-center">
            <div>
                <span :class="[
                    'font-semibold text-2xl',
                    balanceColor ? balanceColor : ''
                    ]"">{{ showBalanceRemain ? formatCurrency(targetAmount - balance) : formatCurrency(balance) }}</span>
                <span class="text-gray-400 mx-1">/</span>
                <span class="font-semibold text-gray-500">{{ formatCurrency(targetAmount)  }}</span>
            </div>
            <span v-if="showPercentage" class="text-sm text-gray-500">{{ percEv }}%</span> 
        </div>
        <UProgress 
            :ui="{
                base: 'bg-gray-50',
                indicator: progressColor ? progressColor : '' 
            }"
            v-bind:model-value="percEv" 
            :color="percEv >= 90 ? 'success' : 'primary'"
            size="md"
        />
    </div>
</template>