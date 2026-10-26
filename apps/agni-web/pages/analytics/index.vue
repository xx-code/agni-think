<script setup  lang="ts">
import type { TabsItem } from '@nuxt/ui';

const items = ref<TabsItem[]>([
    {
        label: 'Periode de depense',
        slot: 'spending-period' as const
    },
    {
        label: 'Trends',
        slot: 'trends' as const
    },
    {
        label: 'Analytique',
        slot: 'analytic' as const
    }
])
</script>

<template>
    <UTabs 
        :items="items"
        class="w-full"
        :ui="{
            list: 'w-fit self-start rounded-full bg-default ring ring-default p-1.5 gap-1',
            indicator: 'rounded-full bg-elevated shadow-none transition-[translate,width] duration-300 ease-[cubic-bezier(0.4,0,0.2,1)]',
            trigger: 'rounded-full px-6 py-2 text-muted font-semibold transition-colors duration-300 data-[state=active]:text-highlighted',
            content: 'data-[state=active]:animate-[tab-content-in_300ms_ease-out]'
        }">
        <template #spending-period>
            <UiSpendingPeriod />
        </template>

        <template #trends>
            <UiTrends />
        </template>

        <template #analytic>
            <UiAnalytics />
        </template>
    </UTabs>
</template>