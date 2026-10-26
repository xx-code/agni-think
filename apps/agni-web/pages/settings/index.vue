<script setup lang="ts">
import type { TabsItem } from "@nuxt/ui";

const navbarItems: TabsItem[] = [
    {
        label: 'Deductions & Taxes',
        slot: 'deductions' as const
    }, 
    {
        label: 'Categories',
        slot: 'categories' as const
    },
    {
        label: 'Tags',
        slot: 'tags' as const
    },
    {
        label: 'Principes Financiers',
        slot: 'principesFinanciers' as const
    },
    {
        label: 'Sources de Revenus',
        slot: 'incomeSources' as const
    },
    {
        label: 'Compte Bancaire Linker',
        slot: 'bankLinker' as const
    },
    {
        label: 'Profile',
        slot: 'profile' as const
    }
]

</script>

<template>
    <div class="p-5 space-y-4">
        <UTabs 
            :items="navbarItems"
            class="w-full"
            :ui="{
                list: 'w-fit self-start rounded-full bg-default ring ring-default p-1.5 gap-1',
                indicator: 'rounded-full bg-elevated shadow-none transition-[translate,width] duration-300 ease-[cubic-bezier(0.4,0,0.2,1)]',
                trigger: 'rounded-full px-6 py-2 text-muted font-semibold transition-colors duration-300 data-[state=active]:text-highlighted',
                content: 'data-[state=active]:animate-[tab-content-in_300ms_ease-out]',
                label: 'text-xs text-wrap'
            }">
            <template #deductions>
                <UiSettingDeductionAndTaxes />
            </template>

            <template #categories>
                <UiSettingCategories />
            </template>

            <template #tags>
                <UiSettingTags />
            </template>

            <template #principesFinanciers>
                <UiSettingPrincipesFinanciers />
            </template>

            <template #incomeSources>
                <UiSettingIncomeSources />
            </template>

            <template #bankLinker>
                <UiSettingAccountBancaireLinker />
            </template>

            <template #profile>
                <UiSettingProfile />
            </template>
        </UTabs>

    </div>    
</template>