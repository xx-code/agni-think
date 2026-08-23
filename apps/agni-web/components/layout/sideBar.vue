<script setup lang="ts">
import type { dashboardSidebarCollapse } from '#build/ui';
import type { NavigationMenuItem } from '@nuxt/ui';

const items: NavigationMenuItem[][] = [
    [ 
        {
            label: 'Vue d\'ensemble',
            icon: 'i-lucide-layout-dashboard',
            to: '/', 
        }
    ],
    [
        { 
            label: 'Portefeuil', 
            icon: 'i-lucide-wallet-minimal',
            open: true,
            children: [
                
                {
                    label: 'Factures',
                    icon: 'i-lucide-banknote',
                    to: '/invoices'
                },
                {
                    label: 'Factures a venir',
                    icon: 'i-lucide-calendar-clock',
                    to: '/schedule-invoices'
                }
            ]
        },
        {
            label: 'Departement Comptable',
            icon: 'i-lucide-landmark',
            children: [
                {
                    label: 'Organizations' ,
                    icon: 'i-lucide-network',
                    to: '/accounting-department/network'
                },
                {
                    label: 'Rapports',
                    icon: 'i-lucide-clipboard-plus',
                    to: '/accounting-department/reports'
                },
                {
                    label: 'Audits',
                    icon: 'i-lucide-shield-cog-corner',
                    to: '/accounting-departement/audits'
                },
                { 
                    label: 'Tendances', 
                    icon: 'i-lucide-circle-gauge', 
                    to: '/trends', 
                },
                {
                    label: 'Analytique',
                    icon: 'i-lucide-chart-candlestick',
                    to: '/analytics', 
                }
            ]
        },
        {
            label: 'Enveloppe', 
            icon: 'i-lucide-mailbox',
            open: true,
            children: [
                { 
                    label: 'Budgets', 
                    to: '/budgets', 
                    icon: 'i-lucide-wallet-cards' 
                },
                { 
                    label: 'Fonds', 
                    to: '/funds', 
                    icon: 'i-lucide-piggy-bank' 
                }
            ]
        }, 
        {
            label: 'Patrimoines',
            icon: 'i-lucide-hotel',
            children: [
                { 
                    label: 'Vue d\'ensemble', 
                    to: '/patrimonies', 
                    icon: 'i-lucide-castle' 
                },
                { 
                    label: 'Provisions', 
                    to: '/provisions', 
                    icon: 'i-lucide-gpu' 
                },
                { 
                    label: 'Pret personnel', 
                    to: '/internal-loans', 
                    icon: 'i-lucide-hand-coins' 
                }
            ]
        },
    ],
    [
        {
            label: 'Parametres',
            icon: 'i-lucide-settings',
            to: '/settings', 
            // children: [
            //     {
            //         label: 'Categories & Tags',
            //         icon: 'i-lucide-tag'
            //     },
            //     {
            //         label: 'Deductions & Taxes',
            //         icon: 'i-lucide-circle-dollar-sign'
            //     },
            //     {
            //         label: 'Sources de revenus',
            //         icon: 'i-lucide-banknote-arrow-up'
            //     },
            //     {
            //         label: 'Principe & Regles',
            //         icon: 'i-lucide-school'
            //     },
            //     {
            //         label: 'Sync compte bancaire',
            //         icon: 'i-lucide-refresh-ccw'
            //     }

            // ]
        }
    ]
] 

</script>

<template>
    <UDashboardSidebar 
        :collapsible="false"
        :ui="{ root: 'border-none hidden md:flex md:w-20 lg:w-64' }">
        <template #header="{ collapsed }">
            <div class="flex justify-between items-center w-full">
                <Logo title="Agni." title-responsive="A." :is-collapsed="collapsed" />
            </div>
        </template>

        <template #default="{ collapsed }">
            <div class="flex flex-col gap-1 px-2">
                <LayoutNavbarGroup 
                    v-for="(item, index) in items"
                    :key="index"
                    :items="item"
                    :divider="index === 1 || index === items.length - 1"
                />
            </div>
        </template>
    </UDashboardSidebar>
</template>