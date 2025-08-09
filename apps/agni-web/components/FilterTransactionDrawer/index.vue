<script setup lang="ts">
import { CalendarDate, DateFormatter } from '@internationalized/date';
import useAccounts from '~/composables/accounts/useAccounts';
import useBudgets from '~/composables/budgets/useBudgets';
import useCategories from '~/composables/categories/useCategories';
import useTags from '~/composables/tags/useTags';
import type { FormFilterTransaction } from '~/types/ui/component';

const emit = defineEmits<{
    (e: 'submit', value: FormFilterTransaction): void
}>();

const { data:accounts } = useAccounts();
const { data:categories } = useCategories();
const { data:tags } = useTags();
const { data:budgets } = useBudgets();

const selectedBudgetIds = ref<string[]>([]);
const selectedCategoryIds = ref<string[]>([]);
const selectedTagIds = ref<string[]>([]);
const selectedAccountIds: Ref<string[]> = ref([]);

const date = shallowRef({
  start: new CalendarDate(new Date().getFullYear(), new Date().getMonth() + 1, new Date().getDate()),
  end: new CalendarDate(new Date().getFullYear(), new Date().getMonth() + 1, new Date().getDate())
})

const filters = ref<{
    filterDate: boolean,
    filterPrice: boolean
}>({
    filterDate: false,
    filterPrice: false 
})

const minAmount = ref<number|undefined>()
const maxAmount = ref<number|undefined>()

function onDateUpdate(startDate?: CalendarDate, endDate?: CalendarDate) {
    date.value.start = startDate || 
        new CalendarDate(new Date().getFullYear(), new Date().getMonth() + 1, new Date().getDate());    
    date.value.end = endDate || 
        new CalendarDate(new Date().getFullYear(), new Date().getMonth() + 1, new Date().getDate()); 
}

function submit() {
    emit('submit', {
        accountIds: selectedAccountIds.value,
        budgetIds: selectedBudgetIds.value,
        categoryIds: selectedCategoryIds.value,
        status: [],
        tagIds: selectedTagIds.value,
        dateEnd:  filters.value.filterDate ? date.value.end.toString() : undefined,
        dateStart: filters.value.filterDate ? date.value.start.toString() : undefined,
        minPrice: filters.value.filterPrice ? minAmount.value : undefined,
        maxPrice: filters.value.filterPrice ? maxAmount.value : undefined
    });
}

function clean() {
    filters.value.filterDate = false;
    filters.value.filterPrice = false;
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
                        placeholder="Compote" 
                        multiple 
                        v-model="selectedAccountIds" 
                        value-key="value"
                        :items="accounts?.items.map(i => ({label: i.title, value: i.id}))"/>
                </div>

                <div>
                    <UInputMenu 
                        placeholder="Categories" 
                        multiple 
                        v-model="selectedCategoryIds" 
                        value-key="value"
                        :items="categories?.items.map(i => ({label: i.title, value: i.id}))"/>
                </div>

                <div>
                    <UInputMenu 
                        placeholder="Tags" 
                        multiple 
                        v-model="selectedTagIds" 
                        value-key="value" 
                        :items="tags?.items.map(i => ({label: i.value, value: i.id}))"/>
                </div> 
                
                <div >
                    <UInputMenu 
                        placeholder="Budgets" 
                        multiple 
                        v-model="selectedBudgetIds" 
                        value-key="value" 
                        :items="budgets?.items.map(i => ({ label: i.title, value: i.id }))"/>
                </div> 
                
                <div>
                    <USwitch v-model="filters.filterDate" />
                    <MultiCalendarSelection 
                        class="mt-2" 
                        :disabled="!filters.filterDate"
                        @submit="onDateUpdate"/>
                </div>

                <div>
                    <USwitch v-model="filters.filterPrice" />
                    <div class="flex gap-1 mt-2">
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