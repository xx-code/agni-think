<script setup lang="ts">
import type { NavigationMenuItem } from '@nuxt/ui'

const items: NavigationMenuItem[][] = [
    [
        {
            label: 'Vue d\'ensemble',
            icon: 'i-lucide-home',
            to: '/'
        }
    ],
    [
        {
            label: 'Portefeuille',
            icon: 'i-lucide-wallet-minimal',
            to: '/wallet'
        },
        {
            label: 'Enveloppe',
            icon: 'i-lucide-mailbox',
            to: '/envelope'
        },
        {
            label: 'Analyse',
            icon: 'i-lucide-landmark',
            to: '/analytics'
        },
        {
            label: 'Patrimoines',
            icon: 'i-lucide-hotel',
            to: '/patrimonies'
        }
    ],
    [
        {
            label: 'Paramètres',
            icon: 'i-lucide-settings',
            to: '/settings'
        }
    ]
]
</script>

<template>
    <UDashboardSidebar
        :collapsible="false"
        :ui="{
            root: 'hidden md:flex w-16 min-w-16 border-r border-default',
            header: 'justify-center px-0 h-16',
            body: 'px-0 py-2'
        }"
    >
        <template #header>
            <Logo title="Agni." title-responsive="A." :is-collapsed="true" />
        </template>

        <template #default>
            <div class="flex min-h-full flex-col items-center gap-1 px-2">
                <LayoutNavbarGroup
                    v-for="(group, index) in items"
                    :key="index"
                    :items="group"
                    :class="{ 'mt-auto': index === items.length - 1 }"
                />
            </div>
        </template>
    </UDashboardSidebar>
</template>