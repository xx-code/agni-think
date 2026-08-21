<script setup lang="ts">
import type { DropdownMenuItem } from '@nuxt/ui';
import { AccountType, getIconAccountType, getLabelAccountType } from '~/types/constants/account';
import type { AccountCard } from '~/types/ui/account';

const { account }  = defineProps<{
    account: AccountCard
}>() 

const emit = defineEmits<{
    update: [id: string]
    delete: [id: string]
}>()

const optionsChart = computed(() => ({
    responsive: true,
    maintainAspectRatio: false,
    interaction: { intersect: false, mode: 'index' },
    plugins: {
        colors: { forceOverride: true},
        legend: { display: false },
        tooltip: {
            enabled: true,
            displayColors: false,
            callbacks: {
            label: (ctx:any) => formatCurrency(ctx.parsed.y),
            title: () => ''
            }
        }
    },
    scales: {
      x: { display: false },       // pas d'axe — c'est une sparkline
      y: { display: false, grace: '10%' }
    },
    elements: {
      line: { borderJoinStyle: 'round' }
    } 
})) 
const dataChart = computed(() => ({
    labels: Array.from({ length: 7 }, (_, index) => `M-${index + 1}`),
    datasets: [{
        data: account.balanceHistory,
        borderColor: account.color,
        fill: true,
        tension: 0.4,                 
        pointRadius: 0,                
        pointHoverRadius: 4,  
        pointHitRadius: 10,
    }]
}))

const actionItems = ref<DropdownMenuItem[][]>([
    [
        {
            class: 'items-center',
            label: 'Modifier',
            icon: 'i-lucide-square-pen',
            onSelect: () => emit('update', account.id)
        },
    ],
    [
        {
            class: 'items-center',
            label: 'Supprimer',
            icon: 'i-lucide-trash',
            color: 'error',
            onSelect: () => emit('delete', account.id)
        }
    ]
])

const showNegativeIndicator = computed(() => {
    if (account.type != AccountType.CreditCard)
        return true 
    return false
})


</script>

<template>
    <div 
        class="flex flex-col gap-2 p-5 cursor-pointer border-l-2 rounded-3xl"
        :style="{borderColor: showNegativeIndicator && account.balance < 0 ? '#fb2c36' : account.color}">
                
        <div class="flex items-start">
            <div class="flex-1">
                <h5 class="text-sm font-semibold text-gray-500">{{ getLabelAccountType(account.type)  }}</h5>
                <h1 class="text-lg font-semibold">{{ account.title }}</h1>
            </div>

            <div>
                <UIcon class="mr-1" :name="getIconAccountType(account.type)" />
                <UDropdownMenu :items="actionItems" >
                    <UButton 
                        @click.stop
                        icon="i-lucide-ellipsis-vertical"
                        variant="ghost"
                        color="neutral"
                    />
                </UDropdownMenu> 
            </div>
            
        </div> 

        <div>
            <span :class="[
                'text-xl font-semibold',
                showNegativeIndicator && account.balance < 0 ? 'text-red-500' : ''
            ]">{{ formatCurrency(account.balance) }}</span>
        </div>

        <div class="h-20">
            <LineChart 
                :data="dataChart"
                :options="optionsChart"
            />
        </div>
    </div>
</template>