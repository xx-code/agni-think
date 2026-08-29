<script lang="ts" setup>
import { getLocalTimeZone } from '@internationalized/date';
import type { DropdownMenuItem } from '@nuxt/ui';
import type { FormFilterTransaction } from '~/types/ui/component';
import type { InvoiceFilter } from '~/types/ui/transaction';

const props = defineProps<{
    accounts: {label: string, value: string}[],
    categories: {label: string, value: string}[]
}>()

const emit = defineEmits<{
    transfer: []
    freeze: []
    syncBank: []
    scanInvoice: []
    filter: []
}>()

const model = defineModel<InvoiceFilter>()

const actionItems = ref<DropdownMenuItem[][]>([
    [
        {
            class: 'items-center',
            label: 'Transfert',
            icon: 'i-lucide-plus',
            color: 'info',
            onSelect: () => emit('transfer')
        },
        {
            class: 'items-center',
            label: 'Freeze Invoice',
            color: 'neutral',
            icon: 'i-lucide-minus',
            onSelect: () => emit('freeze')
        },
    ],
    [
        {
            class: 'items-center',
            label: 'Sync Banque',
            icon: 'i-lucide-arrow-down-to-line',
            onSelect: () => emit('syncBank')
        },
        {
            class: 'items-center',
            label: 'Scanner une facture',
            icon: 'i-lucide-scan-text',
            onSelect: () => emit('scanInvoice')
        },
    ]
]) 

const hasFilters = computed(() => {
    if (!model.value)
        return 

    const ignoredKeys = new Set(['offset', 'limit', 'queryAll'])

    return Object.entries(model.value).some(([key, value]) => {
        if (ignoredKeys.has(key)) return false

        if (Array.isArray(value)) return value.length > 0
        if (typeof value === 'boolean') return value === true
        if (typeof value === 'string') return value.trim().length > 0
        if (typeof value === 'number') return value !== 0

        return value !== undefined && value !== null
    })
})

function onFilter(value: FormFilterTransaction) {
    if(!model.value)
        return 

    model.value = Object.assign(model.value, {...model.value, 
        tagIds: value.tagIds,
        budgetIds: value.budgetIds,
        minAmount: value.minPrice,
        types: value.types,
        maxAmount: value.maxPrice,
        status: value.status,
        offset: 0,
        queryAll: false
    }) 

    emit('filter')
}

function cleanQueryFilter(){
    if(!model.value)
        return 

    model.value = Object.assign(model.value, {...model.value,
        startDate: undefined,
        endDate: undefined,
        accountIds: [],
        categoryIds: [],
    }) 
    emit('filter')
}

</script>

<template>
    <div v-if="model">
        <div class="flex items-center">
            <div class="flex-1 flex items-center flex-wrap gap-2">
                <UiInvoiceSelectMenu 
                    :items="accounts"
                    title="Comptes"
                    v-model="model.accountIds" 
                />

                <UiInvoiceSelectMenu 
                    :items="categories"
                    title="Categories"
                    v-model="model.categoryIds"
                />

                <MultiCalendarSelection 
                    @submit="(start, end) => {
                        if (!model)
                            return

                        model= Object.assign(model, {
                            ...model, 
                            startDate: start?.toDate(getLocalTimeZone()).toISOString(),
                            endDate: end?.toDate(getLocalTimeZone()).toISOString()  
                        })
                    }"
                />
            </div> 
            <div class="flex items-center gap-2">
                <USeparator class="h-full mx-2" orientation="vertical" />
                <UiInvoiceFilterTransactionDrawer @submit="onFilter" />
                <UDropdownMenu :items="actionItems" >
                    <UButton 
                        class="p-2.5"
                        icon="i-lucide-list"
                        size="lg"
                        color="neutral"
                        variant="outline"
                    />
                </UDropdownMenu>  
            </div>
        </div>

        <div 
            v-if="hasFilters"
            class="flex justify-end mt-1">
            <UButton 
                class="p-1 px-4"
                color="neutral"
                variant="ghost"
                label="Effacer Tous"
                @click="cleanQueryFilter"
            />
        </div>
    </div>
</template>