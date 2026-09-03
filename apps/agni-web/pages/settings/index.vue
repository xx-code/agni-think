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
    }
]

</script>

<template>
    <div class="p-5 space-y-4">
        <UTabs 
            :items="navbarItems"
            :ui="{
                label: 'text-xs text-wrap'
            }"
        >
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
        </UTabs>

    </div>    
</template>