<script setup lang="ts">
import type { FundCardGoal, FundGoalState } from '~/types/ui/fund';

const { goal } = defineProps<{
    goal: FundCardGoal
}>()
const emit = defineEmits<{
    update: [id: string]
    delete: [id: string]
}>()

function formatDayGoal(dueDate: Date, status: FundGoalState) {
    if (status === 'ACHIEVED')
        return 'Atteint'

    return getDaysRemaining(dueDate) + " jours"
}

function formatColorByDayGoal(dueDate: Date, status: FundGoalState) {
    if (status === 'ACHIEVED')
        return { text: 'text-green-700', bg: 'bg-green-700'}

    const remainDay = getDaysRemaining(dueDate)
    
    if (remainDay <= 3)
        return { text: 'text-warning-700', bg: 'bg-warning-700'}

    return { text: 'text-gray-700', bg: 'bg-gray-700'}
}

</script>

<template>
    <div class="card-goal p-3 rounded-xl border border-gray-200">
        <div class="card-goal-dropdown hidden gap-2">
            <UButton 
                icon="i-lucide-pen"
                variant="outline"
                size="xs"
                @click="emit('update', goal.id)"
            />
            <UButton 
                icon="i-lucide-trash"
                variant="outline"
                color="error"
                size="xs"
                @click="emit('delete', goal.id)"
            />
        </div>
        <div class="flex-1">
            <div class="flex items-end justify-between">
                <div class="flex-1">
                    <h3 class="font-semibold text-md" :title="goal.description">{{ goal.title }}</h3>
                </div>

                <p :class="[
                        'text-xs text-gray-400 font-semibold', 
                        formatColorByDayGoal(goal.dueDate, goal.status).text
                    ]">
                    {{ formatDayGoal(goal.dueDate, goal.status) }}
                </p>
            </div> 

            <UProgress 
                class="mt-2"
                :ui="{
                    base: 'bg-gray-50',
                }"
                v-bind:model-value="goal.percentage" 
                :color="goal.percentage >= 90 ? 'success' : 'primary'"
                size="sm"
            />
        </div>  
    </div>
</template>

<style scoped lang="scss">
.card-goal:hover {
    .card-goal-dropdown {
        display: flex;
        justify-content: end;
    }
}
</style>