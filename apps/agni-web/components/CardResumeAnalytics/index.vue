<script setup lang="ts">
const { title, amount, description, chipInfo, isPositive, isPercentage } = defineProps<{
    title: string
    amount: number
    description: string
    chipInfo: string
    isPositive: boolean
    isPercentage: boolean
}>()

</script>

<template>
    <UCard>
        <div class="space-y-2">
            <h4 class="text-gray-500 text-md font-bold">{{ title }}</h4>
            <div style="">
                <AmountTitle :amount="amount" :sign="isPercentage ? '%' : '$'"/>
                <UBadge size="md" :color="isPositive ? 'success' : 'error'">
                    <UIcon v-if="isPositive" name="i-lucide-arrow-up" />
                    <UIcon v-else="isPositive" name="i-lucide-arrow-down" />
                    {{ chipInfo }}
                </UBadge>
            </div>
            <div>
                <p class="text-gray-500 text-xs">{{ description }}</p>
            </div>
        </div> 
    </UCard>
</template>