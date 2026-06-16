<script setup lang="ts">
import type { NavigationMenuItem } from '@nuxt/ui';

const items: NavigationMenuItem[][] = [[
    { 
        label: 'Dashboard', 
        icon: 'i-lucide-circle-gauge', 
        to: '/', 
    },
    { 
        label: 'Portefeuil', 
        open: true,
        children: [
            {
                label: 'Vue d\'ensemble',
                icon: 'i-lucide-wallet-minimal',
                to: '/wallets', 
            },
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
                label: 'Analytique',
                icon: 'i-lucide-chart-candlestick',
                to: '/analytics', 
            }
        ]
    },
    {
        label: 'Enveloppe', 
        open: true,
        children: [
            { 
                label: 'Budgets', 
                to: '/budgets', 
                icon: 'i-lucide-wallet-cards' 
            },
            { 
                label: 'Goals', 
                to: '/goals', 
                icon: 'i-lucide-piggy-bank' 
            }
        ]
    }, 
    {
        label: 'Patrimoines',
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
    {
        label: 'Parametres',
        to: '/settings', 
        children: [
            {
                label: 'Categories & Tags',
                icon: 'i-lucide-tag'
            },
            {
                label: 'Deductions & Taxes',
                icon: 'i-lucide-circle-dollar-sign'
            },
            {
                label: 'Sources de revenus',
                icon: 'i-lucide-banknote-arrow-up'
            },
            {
                label: 'Principe & Regles',
                icon: 'i-lucide-school'
            },
            {
                label: 'Sync compte bancaire',
                icon: 'i-lucide-refresh-ccw'
            }

        ]
    }
]] 

</script>

<template>
    <UDashboardSidebar 
        :collapsible="false"
        :ui="{
            root: 'border-none'
        }">
        <template #header="{ collapsed }">
            <div class="flex justify-between items-center w-full">
                <Logo title="Agni." title-responsive="A." :is-collapsed="collapsed" />
            </div>
        </template>

        <template #default="{ collapsed }">
            <UNavigationMenu 
                :collapsed="collapsed"
                v-for="item in items"
                :items="item" 
                orientation="vertical" />
        </template>

    </UDashboardSidebar>
</template>