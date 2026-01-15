<script setup lang="ts">
import { fetchCategories } from '~/composables/categories/useCategories';
import { fetchTransactionPagination } from '~/composables/transactions/useTransactionPagination';
import type { FilterTransactionQuery } from '~/types/api/transaction';
import type { AccountWithDetailType } from '~/types/ui/account';
import ListTransaction from './ListTransaction.vue';

export type SlideQuickViewTransactionType = {
    id: string
    icon: string
    color: string
    category: string
    description: string
    status: string
    date: Date
}

 // 'CreditCard' | 'Checking' | 'Business' | 'Broking' | 'Saving'
const { account } = defineProps<{
    account: AccountWithDetailType 
}>()

const queryFreeze = reactive<FilterTransactionQuery>({
    offset: 0,
    limit: 4,
    accountFilterIds: [account.id],
    categoryFilterIds: [],
    tagFilterIds: [],
    budgetFilterIds: [],
    minPrice: undefined,
    maxPrice: undefined,
    dateStart: undefined,
    dateEnd: undefined,
    isFreeze: true
});
const pageFreeze = ref((queryFreeze.offset / queryFreeze.limit) + 1)

const queryAllTrans = reactive<FilterTransactionQuery>({
    offset: 0,
    limit: 4,
    accountFilterIds: [account.id],
    categoryFilterIds: [],
    tagFilterIds: [],
    budgetFilterIds: [],
    minPrice: undefined,
    maxPrice: undefined,
    dateStart: undefined,
    dateEnd: undefined,
    isFreeze: false
});
const pageTrans = ref((queryFreeze.offset / queryFreeze.limit) + 1)

const { data: categories } = useAsyncData('slide-categories', async () => {
    const categories = await fetchCategories({ queryAll: true, offset: 0, limit: 10})

    return categories.items
})

const getCategory = (categoryId: string) => {
    return categories.value?.find(i => i.id === categoryId)
}

const {data:freezeTransactions, error: errorFreezeTransaction,  refresh:refreshFreezeTransactions } = useAsyncData("slide-freeze-transactions-data", async () => {
    const transactions = await fetchTransactionPagination(queryFreeze) 

    return {
        items: transactions.items.map(i => {
            let cat = getCategory(i.categoryId)

            return ({
                id: i.id, 
                category: cat ? cat.title : "---",
                color: cat?.color ? cat.color : "#ffff",
                date: i.date,
                description: i.description,
                icon: cat ? cat?.icon : "",
                status: i.status

            } satisfies SlideQuickViewTransactionType)
        } ),
        total: transactions.totals
    }

}, {watch: [categories, queryFreeze]});

const {data:transactions, error: errorTransaction,  refresh:refreshTransactions } = useAsyncData("slide-transactions-data", async () => {
    const transactions = await fetchTransactionPagination(queryAllTrans) 

    return {
        items: transactions.items.map(i => {
            let cat = getCategory(i.categoryId)

            return ({
                id: i.id, 
                category: cat ? cat.title : "---",
                color: cat?.color ? cat.color : "#ffff",
                date: i.date,
                description: i.description,
                icon: cat ? cat?.icon : "",
                status: i.status

            } satisfies SlideQuickViewTransactionType)
        } ),
        total: transactions.totals
    }

}, {watch: [categories, queryAllTrans]});
    
const emit = defineEmits<{
    close: [boolean]
}>()

</script>

<template>
    <USlideover
        :close="{ onClick: () => emit('close', false)}">
        <template #body>
            <div>
                <div>
                    <h4 class="mb-2 font-bold">Freeze transactions</h4>
                    <ListTransaction :transactions="freezeTransactions?.items ?? []" />
                    <div class="flex flex-row gap-2 items-baseline-last justify-between">
                        <UPagination 
                            class="mt-3" 
                            v-model:page="pageFreeze" 
                            v-on:update:page="v => queryFreeze.offset = (queryFreeze.limit * (Number(v) - 1))"
                            :items-per-page="queryFreeze.limit"  
                            :total="freezeTransactions?.total" 
                            active-variant="subtle" />
                        <UInputNumber 
                            v-model="queryFreeze.limit" 
                            :min="1" 
                            orientation="vertical" 
                            style="width: 80px;"
                        />
                    </div>
                </div>

                <div>
                    <h4 class="mb-2 font-bold">Transactions</h4>
                    <ListTransaction :transactions="transactions?.items ?? []" />
                    <div class="flex flex-row gap-2 items-baseline-last justify-between">
                        <UPagination 
                            class="mt-3" 
                            v-model:page="pageTrans" 
                            v-on:update:page="v => queryAllTrans.offset = (queryAllTrans.limit * (Number(v) - 1))"
                            :items-per-page="queryAllTrans.limit"  
                            :total="transactions?.total" 
                            active-variant="subtle" />
                        <UInputNumber 
                            v-model="queryAllTrans.limit" 
                            :min="1" 
                            orientation="vertical" 
                            style="width: 80px;"
                        />
                    </div>
                </div>
            </div> 
        </template>
    </USlideover>
</template>