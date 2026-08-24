<script setup lang="ts">
import type { FundCardGoal } from '~/types/ui/fund';

const { goal } = defineProps<{
    goal: FundCardGoal
}>()
</script>

<template>
    <UiCard :class="[
        'flex flex-col gap-3',
        goal.status == 'EXPIRED' ? 'shadow-red-200' : ''
    ]">
        <div class="flex justify-between items-center">
            <h3 class="font-semibold text-md">{{ goal.title }}</h3>
            <span 
                :class="[
                    'px-2.5 py-1 font-medium text-[0.70rem] rounded-full',
                    goal.status == 'EXPIRED' ? 'text-red-700 bg-red-100' : ''
                ]" 
            >{{ getDaysRemaining(goal.dueDate) }} j
            </span>
        </div>

        <UProgress 
            :ui="{
                base: 'bg-gray-50'
            }"
            v-bind:model-value="goal.percentage" 
            :color="goal.status == 'EXPIRED' ? 'error' : 'primary'"
            size="md"
        />

        <div>
            <span class="font-semibold text-gray-500 text-xs">{{ formatCurrency(goal.currentBalance) }}</span>
            <span class="text-gray-400 mx-1">/</span>
            <span class="font-semibold text-gray-500 text-xs">{{ formatCurrency(goal.targetAmount)  }}</span>
        </div>

    </UiCard>
</template>