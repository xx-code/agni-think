<script setup lang="ts">
import { CalendarDate } from '@internationalized/date';
import { fetchAccounts } from '~/composables/accounts/useAccounts';
import { fetchBudgets } from '~/composables/budgets/useBudgets';
import { fetchCategories } from '~/composables/categories/useCategories';
import { fetchTransactionTypes } from '~/composables/internals/useTransactionTypes';
import { fetchTags } from '~/composables/tags/useTags';

import type { FormFilterTransaction } from '~/types/ui/component';

const emit = defineEmits<{
    (e: 'submit', value: FormFilterTransaction): void
}>();

const { data: utils } = useAsyncData('utils+edit-invoices', async () => {
    const query = {offset: 0, limit: 0, queryAll: true, isSystem: false}
    const [ categories, tags, budgets, accounts, transactionTypes ] = await Promise.all([
        fetchCategories(query),
        fetchTags(query),
        fetchBudgets(query),
        fetchAccounts(query),
        fetchTransactionTypes()
    ])

    return {
        categories,
        tags,
        budgets,
        accounts,
        transactionTypes
    }
})

// TODO: Refactor
const selectedBudgetIds = ref<string[]>([]);
const selectedCategoryIds = ref<string[]>([]);
const selectedTagIds = ref<string[]>([]);
const selectedAccountIds: Ref<string[]> = ref([]);
const selectedStatus = ref<string>()
const selectedTypeTransaction = ref<string[]>([])

const date = shallowRef({
  start: new CalendarDate(new Date().getUTCFullYear(), new Date().getUTCMonth() + 1, new Date().getUTCDate()),
  end: new CalendarDate(new Date().getUTCFullYear(), new Date().getUTCMonth() + 1, new Date().getUTCDate())
})

const filters = ref<{
    filterDate: boolean,
    filterPrice: boolean,
    filterStatus: boolean
}>({
    filterDate: false,
    filterPrice: false,
    filterStatus: false,
})

const minAmount = ref<number|undefined>()
const maxAmount = ref<number|undefined>()

function onDateUpdate(startDate?: CalendarDate, endDate?: CalendarDate) {
    date.value.start = startDate || 
        new CalendarDate(new Date().getUTCFullYear(), new Date().getUTCMonth() + 1, new Date().getUTCDate());    
    date.value.end = endDate || 
        new CalendarDate(new Date().getUTCFullYear(), new Date().getUTCMonth() + 1, new Date().getUTCDate()); 
}

function submit() {
    emit('submit', {
        accountIds: selectedAccountIds.value,
        budgetIds: selectedBudgetIds.value,
        categoryIds: selectedCategoryIds.value,
        types:  selectedTypeTransaction.value,
        status: filters.value.filterStatus ? selectedStatus.value : undefined, 
        tagIds: selectedTagIds.value,
        dateEnd:  filters.value.filterDate ? date.value.end : undefined,
        dateStart: filters.value.filterDate ? date.value.start : undefined,
        minPrice: filters.value.filterPrice ? minAmount.value : undefined,
        maxPrice: filters.value.filterPrice ? maxAmount.value : undefined
    });
}

function clean() {
    filters.value.filterDate = false;
    filters.value.filterPrice = false;
    filters.value.filterStatus = false;
    selectedTypeTransaction.value = [];
    selectedAccountIds.value = [];
    selectedBudgetIds.value = [];
    selectedCategoryIds.value = [];
    selectedTagIds.value = [];
    submit();
}

</script>

<template>
    <UDrawer direction="right" inset >
        <UButton color="neutral" variant="outline" icon="i-lucide-sliders-horizontal" size="xl" label="Filtres"/>
        <template #content>
            <div class="min-w-96 min-h-96 size-full m-4 space-y-5">
                <div>
                    <UInputMenu 
                        placeholder="Compte" 
                        multiple 
                        v-model="selectedAccountIds" 
                        value-key="value"
                        :items="utils?.accounts.items.map(i => ({label: i.title, value: i.id}))"/>
                </div>

                <div>
                    <UInputMenu 
                        placeholder="Categories" 
                        multiple 
                        v-model="selectedCategoryIds" 
                        value-key="value"
                        :items="utils?.categories.items.map(i => ({label: i.title, value: i.id}))"/>
                </div>

                <div>
                    <UInputMenu 
                        placeholder="Tags" 
                        multiple 
                        v-model="selectedTagIds" 
                        value-key="value" 
                        :items="utils?.tags.items.map(i => ({label: i.value, value: i.id}))"/>
                </div> 
                
                <div >
                    <UInputMenu 
                        placeholder="Budgets" 
                        multiple 
                        v-model="selectedBudgetIds" 
                        value-key="value" 
                        :items="utils?.budgets.items.map(i => ({ label: i.title, value: i.id }))"/>
                </div> 

                <div >
                    <UInputMenu 
                        placeholder="Type de transaction" 
                        multiple 
                        v-model="selectedTypeTransaction" 
                        value-key="value" 
                        :items="utils?.transactionTypes.map(i => ({ label: i.value, value: i.id }))"/>
                </div>
                
                <div>
                    <USwitch v-model="filters.filterDate" label="Fitlrer par date" />
                    <MultiCalendarSelection 
                        class="mt-2" 
                        :disabled="!filters.filterDate"
                        @submit="onDateUpdate"/>
                </div>

                <div >
                    <USwitch v-model="filters.filterStatus" label="Fitlrer par status" />
                    <UTabs class="mt-2" default-value="Complete"
                        v-model="selectedStatus" value-key="value" 
                        v-if="filters.filterStatus"
                        :items="[{label: 'Complete', value: 'Complete' }, { label: 'Pending', value: 'Pending'}]" />
                </div> 

                <div>
                    <USwitch v-model="filters.filterPrice" label="Filtrer par somme" />
                    <div  v-if="filters.filterPrice" class="flex gap-1 mt-2">
                        <UInput 
                            placeholder="Min" 
                            v-model="minAmount" 
                            type="number" :min="0" :disabled="!filters.filterPrice"/>
                        <UInput 
                            placeholder="Max" 
                            v-model="maxAmount" 
                            type="number" :min="minAmount" :disabled="!filters.filterPrice" />
                    </div>
                </div>

                <div class="space-x-3">
                    <UButton 
                        label="Nettoyer"
                        variant="outline"
                        @click="clean" />

                    <UButton 
                        label="Filtrer"
                        @click="submit" />
                </div>
            </div>
        </template>
    </UDrawer>
</template>