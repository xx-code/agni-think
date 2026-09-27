<script lang="ts" setup>
import { getLocalTimeZone } from '@internationalized/date';
import type { DropdownMenuItem } from '@nuxt/ui';
import type { FormFilterTransaction, MultiCalendarSelection } from '~/types/ui/component';

const props = defineProps<{
    accounts: {label: string, value: string}[],
    categories: {label: string, value: string}[]
}>()

const categoryIds = ref<string[]>([])
const accountIds = ref<string[]>([])
const dateRange = shallowRef<MultiCalendarSelection>({
    start: undefined,
    end: undefined
});

const emit = defineEmits<{
    transfer: []
    freeze: []
    syncBank: []
    scanInvoice: []
    filter: [
        {
            accountIds?: string[],
            categoryIds?: string[],
            budgetIds?: string[],
            tagIds?: string[],
            types?: string[],
            mouvement?: string,
            maxAmount?: number,
            minAmount?: number,
            status?: string
            startDate?: string,
            endDate?: string
        }
    ]
}>()

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
    return categoryIds.value.length > 0 || accountIds.value.length > 0  || dateRange.value.start !== undefined || dateRange.value.end !== undefined
})

function onFilter(value: FormFilterTransaction) {
    emit('filter',{ 
        categoryIds: categoryIds.value,
        accountIds: accountIds.value,
        tagIds: value.tagIds,
        budgetIds: value.budgetIds,
        minAmount: value.minPrice,
        startDate: dateRange.value.start?.toDate(getLocalTimeZone()).toISOString(),
        endDate: dateRange.value.end?.toDate(getLocalTimeZone()).toISOString(),
        types: value.types,
        maxAmount: value.maxPrice,
        status: value.status
    }) 

}

function cleanQueryFilter(){
    categoryIds.value = []
    accountIds.value = []
    dateRange.value = {
        start: undefined,
        end: undefined
    }
    emit('filter', {
        accountIds: [],
        categoryIds: [],
        tagIds: [],
        budgetIds: [],
        types: [],
        mouvement: undefined,
        status: undefined,
        minAmount: undefined,
        maxAmount: undefined,
        startDate: undefined,
        endDate: undefined
    })
}

</script>

<template>
    <div>
        <div class="flex items-center">
            <div class="flex-1 flex items-center flex-wrap gap-2">
                <UiInvoiceSelectMenu 
                    :items="accounts"
                    title="Comptes"
                    v-model="accountIds"
                    v-on:update:model-value="ids => {
                        emit('filter', {
                            accountIds: ids
                        })
                    }"
                />

                <UiInvoiceSelectMenu 
                    :items="categories"
                    title="Categories"
                    v-model="categoryIds"
                    v-on:update:model-value="ids => {
                        emit('filter', {
                            categoryIds: ids
                        })
                    }"
                />

                <MultiCalendarSelection 
                    v-model="dateRange"
                    v-on:update:model-value="dateRange => {
                        emit('filter', {
                            startDate: dateRange?.start?.toDate(getLocalTimeZone()).toISOString(),
                            endDate: dateRange?.end?.toDate(getLocalTimeZone()).toISOString(),
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