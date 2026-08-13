<script setup lang="ts">
import type { DropdownMenuItem } from '@nuxt/ui';
import type { FundCard } from '~/types/ui/fund';

const { fund } = defineProps<{
    fund: FundCard
}>()

const emit = defineEmits<{
    update: [id: string]
    delete: [id: string]
    increaseAmount: [id: string]
    decreaseAmount: [id: string]
    addGoal: [id: string]
}>()

const percEv = roundNumber(computePercentage(fund.target, fund.balance))

const actionItems = ref<DropdownMenuItem[][]>([
    [
        {
            class: 'items-center',
            label: 'Modifier',
            icon: 'i-lucide-square-pen',
            onSelect: () => emit('update', fund.id)
        },
    ],
    [
        {
            class: 'items-center',
            label: 'Ajouter',
            icon: 'i-lucide-plus',
            color: 'success',
            onSelect: () => emit('increaseAmount', fund.id)
        },
        {
            class: 'items-center',
            label: 'Retirer',
            color: 'warning',
            icon: 'i-lucide-minus',
            onSelect: () => emit('decreaseAmount', fund.id)
        },
    ],
    [
        {
            class: 'items-center',
            label: 'Supprimer',
            icon: 'i-lucide-trash',
            color: 'error',
            onSelect: () => emit('delete', fund.id)
        }
    ]
]) 

</script>

<template>
    <div class="p-5 rounded-xl bg-white border border-gray-200 h-full cursor-pointer hover:shadow-sm">
        <div class="flex items-start gap-1 mb-4">
            <div class="flex-1 min-w-0">
                <h3 class="font-bold text-lg truncate" :title="fund.title">{{ fund.title }}</h3>
                <p class="truncate text-sm text-neutral-700 tracking-tighter" 
                    :title="fund.description">{{ fund.description }}</p>
            </div>
            <span 
                class="px-2.5 py-1 font-medium text-[0.70rem] text-primary-700 rounded-full" 
                style="background-color: rgba(103, 85, 215, 0.1);">{{ percEv }}%</span>
            <UDropdownMenu :items="actionItems" @click.stop>
                <UButton 
                    icon="i-lucide-ellipsis-vertical"
                    variant="ghost"
                    color="neutral"
                />
            </UDropdownMenu> 
        </div>

        <div>
            <div class="mb-2">
                <span class="font-semibold text-2xl text-green-700">{{ formatCurrency(fund.balance) }}</span>
                <span class="text-gray-400 mx-1">/</span>
                <span class="font-semibold text-gray-500">{{ formatCurrency(fund.target)  }}</span>
            </div>
            <UProgress 
                :ui="{
                    base: 'bg-gray-50'
                }"
                v-bind:model-value="percEv" 
                :color="percEv >= 90 ? 'success' : 'primary'"
                size="md"
            />
        </div>

        <USeparator class="w-full mt-4 mb-2" /> 

        <div class="m-auto flex  items-center">
            <div class="flex-1">
                <div class="flex text-gray-400 text-sm gap-1 items-center" v-if="fund.goalSummary">
                    <UIcon name="i-lucide-flag" />
                    <span>{{ fund.goalSummary.numberGoal }} Jalons</span>
                    <span>·</span>
                    <span>{{ getDaysRemaining(fund.goalSummary.nextDueDate) }} jours</span>
                </div>
                <UButton 
                    v-else
                    icon="i-lucide-plus"
                    label="Ajouter un jalon"
                    color="neutral"
                    variant="ghost"
                    @click="emit('addGoal', fund.id)"
                />
            </div>

            <p class="text-primary-800 text-sm font-semibold">
                {{ formatCurrency(fund.target - fund.balance) }}
            </p>
        </div>
    </div>
</template>