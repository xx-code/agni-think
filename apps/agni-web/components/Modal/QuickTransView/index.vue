<script setup lang="ts">
import { fetchCategories } from '~/composables/categories/useCategories';
import { fetchTransactionPagination } from '~/composables/invoices/useTransactionPagination';
import type { FilterTransactionQuery } from '~/types/api/transaction';
import type { AccountWithDetailType } from '~/types/ui/account';
import ListTransaction from './ListTransaction.vue';
import { fetchBalance } from '~/composables/invoices/useBalance';
import { fetchTags } from '~/composables/tags/useTags';
import { fetchBudgets } from '~/composables/budgets/useBudgets';

export type SlideQuickViewTransactionType = {
    id: string
    icon?: string
    color?: string
    category?: string
    description: string
    status: string
    mouvement: string
    type: string
    date: Date
    subTotal: number
    total: number
    records?: {
        id: string
        description: string
        amount: number
        category: {
            id: string
            icon: string
            color: string
            title: string
        }
        tags: {
            id: string
            value: string
            color: string
        }[]
        budgets: {
            id: string
            title: string
        }[]
    }[]
    deductions?: {
        name: string
        amount: number
    }[]
}

const { account } = defineProps<{
    account: AccountWithDetailType 
}>();

// État actif (freeze ou all)
const activeTab = ref<'freeze' | 'all'>('all');

const queryFreeze = reactive<FilterTransactionQuery>({
    offset: 0,
    limit: 5,
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
const pageFreeze = ref(1);

const queryAllTrans = reactive<FilterTransactionQuery>({
    offset: 0,
    limit: 5,
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
const pageTrans = ref(1);

const { data: utils } = useAsyncData('slide-categories', async () => {
    const categories = await fetchCategories({ queryAll: true, offset: 0, limit: 100 });
    const tags = await fetchTags({ queryAll: true, offset: 0, limit: 100 });
    const budgets = await fetchBudgets({ queryAll: true, offset: 0, limit: 100 });

    return { categories: categories.items, tags: tags.items, budgets: budgets.items };
});

const { data: balances } = useAsyncData('slide-categories+balance', async () => {
    const res = await fetchBalance(queryAllTrans);
    return res;
});

const getCategory = (categoryId: string) => {
    return utils.value?.categories.find(i => i.id === categoryId);
};

const getTag = (tagId: string) => {
    return utils.value?.tags.find(i => i.id === tagId);
};

const getBudget = (budgetId: string) => {
    return utils.value?.budgets.find(i => i.id === budgetId);
}

// TODO: Duplicate
const { data: freezeTransactions, refresh: refreshFreezeTransactions } = useAsyncData(
    "slide-freeze-transactions-data", 
    async () => {
        const transactions = await fetchTransactionPagination(queryFreeze);

        return {
            items: transactions.items.map(i => {
                return {
                    id: i.id,
                    description: `${i.records?.length || 0} article${(i.records?.length || 0) > 1 ? 's' : ''}`,
                    date: i.date,
                    status: i.status,
                    type: i.type,
                    mouvement: i.mouvement,
                    subTotal: i.subTotal,
                    total: i.total,
                    records: i.records?.map(record => ({
                        id: record.id,
                        description: record.description,
                        amount: record.amount,
                        category: {
                            id: record.categoryId,
                            icon: getCategory(record.categoryId)?.icon || 'i-lucide-circle',
                            color: getCategory(record.categoryId)?.color || '#808080',
                            title: getCategory(record.categoryId)?.title || 'Sans catégorie'
                        },
                        tags: record.tagRefs?.map(tagId => {
                            return {
                                id: tagId,
                                value: getTag(tagId)?.value || '',
                                color: getTag(tagId)?.color || '#808080' 
                            };
                        }) || [],
                        budgets: record.budgetRefs?.map(budgetId => {
                            return {
                                id: budgetId,
                                title: getBudget(budgetId)?.title || ''
                            };
                        }) || []
                    })),
                    deductions: i.deductions.map(i => ({ name: i.id, amount: i.amount}))
                } satisfies SlideQuickViewTransactionType;
            }),
            total: transactions.totals
        };
    }, 
    { watch: [utils, queryFreeze] }
);

const { data: transactions, refresh: refreshTransactions } = useAsyncData(
    "slide-transactions-data", 
    async () => {
        const transactions = await fetchTransactionPagination(queryAllTrans);

        return {
            items: transactions.items.map(i => {
                // Pour les nouvelles transactions avec records
                return {
                    id: i.id,
                    date: i.date,
                    status: i.status,
                    type: i.type,
                    subTotal: i.subTotal,
                    description: `${i.records?.length || 0} article${(i.records?.length || 0) > 1 ? 's' : ''}`,
                    total: i.total,
                    mouvement: i.mouvement,
                    records: i.records?.map(record => ({
                        id: record.id,
                        description: record.description,
                        amount: record.amount,
                        category: {
                            id: record.categoryId,
                            icon: getCategory(record.categoryId)?.icon || 'i-lucide-circle',
                            color: getCategory(record.categoryId)?.color || '#808080',
                            title: getCategory(record.categoryId)?.title || 'Sans catégorie'
                        },
                        tags: record.tagRefs?.map(tagId => {
                            return {
                                id: tagId,
                                value: getTag(tagId)?.value || '',
                                color: getTag(tagId)?.color || '#808080' 
                            };
                        }) || [],
                        budgets: record.budgetRefs?.map(budgetId => {
                            return {
                                id: budgetId,
                                title: getBudget(budgetId)?.title || ''
                            };
                        }) || []
                    })),
                    deductions: i.deductions.map(i => ({ name: i.id, amount: i.amount}))
                } satisfies SlideQuickViewTransactionType;
            }),
            total: transactions.totals
        };
    }, 
    { watch: [utils, queryAllTrans] }
);

const emit = defineEmits<{
    close: [boolean]
}>();

function getAccountTypeIcon(type: string) {
    const icons = {
        'CreditCard': 'i-lucide-credit-card',
        'Checking': 'i-lucide-landmark',
        'Business': 'i-lucide-briefcase',
        'Broking': 'i-lucide-trending-up',
        'Saving': 'i-lucide-piggy-bank'
    };
    return icons[type as keyof typeof icons] || 'i-lucide-wallet';
}

</script>

<template>
    <USlideover :close="{ onClick: () => emit('close', false) }">
        <template #body>
            <div class="space-y-6">
                <!-- En-tête du compte -->
                <div class="bg-gradient-to-br from-primary-50 to-primary-100 dark:from-gray-800 dark:to-gray-900 rounded-xl p-6">
                    <div class="flex items-start gap-4">
                        <div 
                            class="flex items-center justify-center rounded-xl bg-white dark:bg-gray-800 shadow-lg"
                            style="width: 56px; height: 56px;"
                        >
                            <UIcon 
                                :name="getAccountTypeIcon(account.type)" 
                                class="text-2xl text-primary-600"
                            />
                        </div>

                        <div class="flex-1">
                            <h2 class="text-2xl font-bold text-gray-900 dark:text-white">
                                {{ account.title }}
                            </h2>
                            <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
                                {{ account.type }}
                            </p>

                            <!-- Soldes -->
                            <div class="grid grid-cols-2 gap-4 mt-4">
                                <div>
                                    <p class="text-xs text-gray-600 dark:text-gray-400">Solde disponible</p>
                                    <p class="text-lg font-semibold text-gray-900 dark:text-white">
                                        {{ formatCurrency(account.balance) }}
                                    </p>
                                </div>
                                <div v-if="account.freezedBalance > 0">
                                    <p class="text-xs text-gray-600 dark:text-gray-400">Solde gelé</p>
                                    <p class="text-lg font-semibold text-amber-600">
                                        {{ formatCurrency(account.freezedBalance) }}
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Statistiques de la période affichée -->
                <div 
                    v-if="balances"
                    class="grid grid-cols-3 gap-3"
                >
                    <div class="bg-green-50 dark:bg-green-900/20 rounded-lg p-3">
                        <p class="text-xs text-green-700 dark:text-green-400 font-medium">Revenus</p>
                        <p class="text-lg font-bold text-green-600 dark:text-green-500">
                            {{ formatCurrency(balances.income) }}
                        </p>
                    </div>
                    <div class="bg-red-50 dark:bg-red-900/20 rounded-lg p-3">
                        <p class="text-xs text-red-700 dark:text-red-400 font-medium">Dépenses</p>
                        <p class="text-lg font-bold text-red-600 dark:text-red-500">
                            {{ formatCurrency(balances.spend) }}
                        </p>
                    </div>
                    <div class="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-3">
                        <p class="text-xs text-blue-700 dark:text-blue-400 font-medium">Net</p>
                        <p 
                            class="text-lg font-bold"
                            :class="(balances.income - balances.spend) >= 0 ? 'text-green-600 dark:text-green-500' : 'text-red-600 dark:text-red-500'"
                        >
                            {{ formatCurrency((balances.income - balances.spend)) }}
                        </p>
                    </div>
                </div>

                <!-- Tabs -->
                <div class="flex gap-2 border-b border-gray-200 dark:border-gray-700">
                    <button
                        @click="activeTab = 'all'"
                        :class="[
                            'px-4 py-2 font-medium text-sm transition-colors relative',
                            activeTab === 'all'
                                ? 'text-primary-600 dark:text-primary-400'
                                : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-200'
                        ]"
                    >
                        Toutes les transactions
                        <div 
                            v-if="activeTab === 'all'"
                            class="absolute bottom-0 left-0 right-0 h-0.5 bg-primary-600 dark:bg-primary-400"
                        />
                    </button>
                    <button
                        v-if="freezeTransactions?.items && freezeTransactions.items.length > 0"
                        @click="activeTab = 'freeze'"
                        :class="[
                            'px-4 py-2 font-medium text-sm transition-colors relative flex items-center gap-2',
                            activeTab === 'freeze'
                                ? 'text-amber-600 dark:text-amber-400'
                                : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-200'
                        ]"
                    >
                        <span>Transactions gelées</span>
                        <UBadge 
                            :label="String(freezeTransactions.total)"
                            class="text-amber-600"
                            variant="subtle"
                            size="xs"
                        />
                        <div 
                            v-if="activeTab === 'freeze'"
                            class="absolute bottom-0 left-0 right-0 h-0.5 bg-amber-600 dark:bg-amber-400"
                        />
                    </button>
                </div>

                <!-- Liste des transactions gelées -->
                <div v-if="activeTab === 'freeze' && freezeTransactions?.items && freezeTransactions.items.length > 0">
                    <div class="flex items-center justify-between mb-3">
                        <h4 class="font-semibold text-gray-900 dark:text-white">
                            Transactions gelées ({{ freezeTransactions.total }})
                        </h4>
                    </div>

                    <ListTransaction :transactions="freezeTransactions.items" />

                    <div class="flex flex-row gap-2 items-center justify-between mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
                        <UPagination 
                            v-model:page="pageFreeze" 
                            @update:page="v => { queryFreeze.offset = (queryFreeze.limit * (Number(v) - 1)) }"
                            :items-per-page="queryFreeze.limit"  
                            :total="freezeTransactions.total" 
                            active-variant="subtle" 
                        />
                        <div class="flex items-center gap-2">
                            <span class="text-xs text-gray-600 dark:text-gray-400">Par page:</span>
                            <UInputNumber 
                                v-model="queryFreeze.limit" 
                                :min="1"
                                :max="20"
                                orientation="vertical" 
                                size="sm"
                                class="w-16"
                            />
                        </div>
                    </div>
                </div>

                <!-- Liste de toutes les transactions -->
                <div v-if="activeTab === 'all' && transactions?.items && transactions.items.length > 0">
                    <div class="flex items-center justify-between mb-3">
                        <h4 class="font-semibold text-gray-900 dark:text-white">
                            Toutes les transactions ({{ transactions.total }})
                        </h4>
                    </div>

                    <ListTransaction :transactions="transactions.items" />

                    <div class="flex flex-row gap-2 items-center justify-between mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
                        <UPagination 
                            v-model:page="pageTrans" 
                            @update:page="v => { queryAllTrans.offset = (queryAllTrans.limit * (Number(v) - 1)) }"
                            :items-per-page="queryAllTrans.limit"  
                            :total="transactions.total" 
                            active-variant="subtle" 
                        />
                        <div class="flex items-center gap-2">
                            <span class="text-xs text-gray-600 dark:text-gray-400">Par page:</span>
                            <UInputNumber 
                                v-model="queryAllTrans.limit" 
                                :min="1"
                                :max="20"
                                orientation="vertical" 
                                size="sm"
                                class="w-16"
                            />
                        </div>
                    </div>
                </div>

                <!-- État vide -->
                <div 
                    v-if="(activeTab === 'all' && (!transactions?.items || transactions.items.length === 0)) ||
                          (activeTab === 'freeze' && (!freezeTransactions?.items || freezeTransactions.items.length === 0))"
                    class="text-center py-12"
                >
                    <UIcon 
                        name="i-lucide-inbox" 
                        class="w-12 h-12 mx-auto text-gray-400 mb-3"
                    />
                    <p class="text-gray-600 dark:text-gray-400">
                        Aucune transaction trouvée
                    </p>
                </div>
            </div> 
        </template>
    </USlideover>
</template>