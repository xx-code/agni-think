<script setup lang="ts">
import type { TableColumn, TableRow } from "@nuxt/ui";
import { fetchAccounts } from "~/composables/accounts/useAccounts";
import { fetchBudgets } from "~/composables/budgets/useBudgets";
import { fetchCategories } from "~/composables/categories/useCategories";
import { fetchTags } from "~/composables/tags/useTags";
import { fetchInvoicePagination } from "~/composables/invoices/useTransactionPagination";
import { fetchBalance } from "~/composables/invoices/useBalance";
import useDeleteTransaction from "~/composables/invoices/useDeleteTransaction";
import type { EditInvoiceType, InvoiceTableType, InvoiceType, TransactionTableType, TransactionType } from "~/types/ui/transaction";
import { fetchInvoice } from "~/composables/invoices/useTransaction";
import useUpdateInvoice from "~/composables/invoices/useUpdateTransaction";
import useCreateInvoice from "~/composables/invoices/useCreateTransaction";
import type { FormFilterTransaction } from "~/types/ui/component";
import useCompleteInvoice from "~/composables/invoices/useCompleteTransaction";
import { getLocalTimeZone } from "@internationalized/date";
import type { QueryInvoice } from "~/types/api/transaction";
import type { QueryFilterRequest } from "~/types/api";
import { ModalEditInvoice } from "#components";

const toast = useToast();

const page = ref(1);
const query = reactive<QueryFilterRequest & QueryInvoice>({
    offset: 0,
    limit: 8,
    queryAll: false,
    accountIds: [],
    categoryIds: [],
    tagIds: [],
    budgetIds: [],
    minAmount: undefined,
    maxAmount: undefined,
    mouvement: undefined,
    endDate: undefined,
    startDate: undefined,
    isFreeze: false
});

const { data: utils, refresh: refreshUtils, error: errorUtils } = useAsyncData('utils-transactions', async () => {
    const [ accounts, categories, deductions, budgets, tags] = await Promise.all([
        fetchAccounts({ offset: 0, limit: 0, queryAll: true }),
        fetchCategories({ offset: 0, limit: 0, queryAll: true }),
        fetchDeductions({ offset: 0, limit: 0, queryAll: true }),
        fetchBudgets({ offset: 0, limit: 0, queryAll: true }),
        fetchTags({ offset: 0, limit: 0, queryAll: true })
    ])

    return {
        accounts: accounts.items, 
        categories: categories.items,
        budgets: budgets.items,
        deductions: deductions.items,
        tags: tags.items
    }

})

const getCategory = (id: string) => utils.value?.categories.find(i => id === i.id)
const getTag = (id: string) => utils.value?.tags.find(i => id === i.id)
const getBudget = (id: string) => utils.value?.budgets.find(i => id === i.id)
const getDeduction = (id: string) => utils.value?.deductions.find(i => id === i.id)

function calculateDeductionAmount(deduction: { deductionId: string; amount: number }) {
    const deductionType = getDeduction(deduction.deductionId);
    if (!deductionType) return 0;
    
    if (deductionType.mode === 'Flat') {
        return deduction.amount || 0;
    } else if (deductionType.mode === 'Rate') {
        return  (deduction.amount || 0) / 100;
    }
    
    return 0;
}

const { data, error, refresh, status } = useAsyncData(`transactions-${JSON.stringify(query)}`, async () => {

    const [transactions, balance ] = await Promise.all([
        fetchInvoicePagination(query),
        fetchBalance(query),
    ])


    

    return {
        transactions: transactions.items.map(i => ({
            id: i.id,
            accountId: i.accountId,
            date: i.date,
            type: i.type,
            status: i.status,
            subTotal: i.subTotal,
            total: i.total,
            description: `${i.transactions.length} article${i.transactions.length > 1 ? 's' : ''}`,
            mouvement: i.mouvement,
            transactions: i.transactions.map(record => ({
                id: record.id,
                description: record.description,
                amount: record.amount,
                category: {
                    id: record.categoryId,
                    icon: getCategory(record.categoryId)?.icon || 'i-lucide-circle',
                    color: getCategory(record.categoryId)?.color || '#808080',
                    title: getCategory(record.categoryId)?.title || 'Sans catégorie',
                },
                tags: record.tagRefs.map(tagId => ({
                    id: tagId,
                    value: getTag(tagId)?.value || '',
                    color: getTag(tagId)?.color || ''
                })),
                budgets: record.budgetRefs.map(budgetId => ({
                    id: budgetId,
                    title: getBudget(budgetId)?.title || ''
                }))
            } satisfies TransactionTableType)),
            deductions: i.deductions.map(d => ({
                name: getDeduction(d.id)?.description || '', // ou autre nom si disponible
                amount: calculateDeductionAmount({deductionId: d.id, amount: d.amount}) 
            }))
        } satisfies InvoiceTableType)),

        total: transactions.total,
        balance: balance.balance,
        income: balance.income,
        spends: balance.spend
    }
}, { watch: [utils, query] });


const expandedState = ref<Record<string, boolean>>({})

const overlay = useOverlay()
const modalInvoice = overlay.create(ModalEditInvoice);

async function onSubmitInvoice(value: EditInvoiceType, oldValue?: InvoiceType) {
    try {
        if (oldValue) {
            const transactionRemovedIds = oldValue.transactions.filter(i => !value.transactions.find(
                v => 
                    v.amount === i.amount && 
                    v.categoryId === i.categoryId &&
                    v.budgetIds.length === i.budgetRefs.length &&
                    v.budgetIds.every(b => i.budgetRefs.includes(b)) &&
                    v.tagIds.length === i.tagRefs.length &&
                    v.tagIds.every(t => i.tagRefs.includes(t)) &&
                    v.description === i.description
            )).map(i => i.id)

            const transactionAdded = value.transactions.filter(i => !oldValue.transactions.find(
                v => 
                    v.amount === i.amount && 
                    v.categoryId === i.categoryId &&
                    v.budgetRefs.length === i.budgetIds.length &&
                    v.budgetRefs.every(b => i.budgetIds.includes(b)) &&
                    v.tagRefs.length === i.tagIds.length &&
                    v.tagRefs.every(t => i.tagIds.includes(t)) &&
                    v.description === i.description
            ))

            await useUpdateInvoice(oldValue.id, {
                addTransactions: transactionAdded, 
                mouvement: value.mouvement,
                removeTransactionIds: transactionRemovedIds,
                deductions: value.deductions.map(i => ({ deductionId: i.deductionId, amount: i.amount})),
                accountId: value.accountId,
                date: value.date.toDate(getLocalTimeZone()).toISOString(),
                type: value.type
            });
        } else {
            await useCreateInvoice({
                accountId: value.accountId,
                date: value.date.toDate(getLocalTimeZone()).toISOString(),
                mouvement: value.mouvement,
                type: value.type,
                status: value.state,
                transactions: value.transactions.map(i => ({
                    amount: i.amount,
                    categoryId: i.categoryId,
                    budgetIds: i.budgetIds,
                    description: i.description,
                    tagIds: i.tagIds
                })),
                deductions: value.deductions.map(i => ({ deductionId: i.deductionId, amount: i.amount})),
            });
        }
        refresh()
    } catch (err) {
        toast.add({
            title: 'Erreur',
            description: 'Erreur lors de la soumission de la transaction: ' + err,
            color: 'error'
        })
    }
}

async function openInvoice(transactionId?: string) {
    let invoice: any | undefined;
    if (transactionId)
        invoice = await fetchInvoice(transactionId);

    modalInvoice.open({
        invoice: invoice,
        onSubmit: onSubmitInvoice
    });
};

const onDelete = async (id: string) => {
    await useDeleteTransaction(id)
    refresh()
}

function onFilter(value: FormFilterTransaction) {
    query.categoryIds = value.categoryIds
    query.tagIds = value.tagIds
    query.accountIds = value.accountIds
    query.budgetIds = value.budgetIds
    query.endDate = value.dateEnd ? value.dateEnd.toDate(getLocalTimeZone()).toISOString() : undefined
    query.startDate = value.dateStart ? value.dateStart.toDate(getLocalTimeZone()).toISOString() : undefined
    query.minAmount = value.minPrice
    query.types = value.types
    query.maxAmount = value.maxPrice
    query.status = value.status
    query.offset = 0
    page.value = 1

    expandedState.value = {}
}

const UIcon = resolveComponent('UIcon');
const UButton = resolveComponent('UButton');
const UDropdownMenu = resolveComponent('UDropdownMenu');
const UBadge = resolveComponent('UBadge');

// Colonnes pour la table groupée
const tableColumn: TableColumn<InvoiceTableType>[] = [
    {
        id: 'expand',
        cell: ({ row }) => {
            return h(UButton, {
                color: 'neutral',
                variant: 'ghost',
                icon: 'i-lucide-chevron-down',
                square: true,
                'aria-label': 'Expand',
                ui: {
                    leadingIcon: [
                        'transition-transform duration-200',
                        row.getIsExpanded() ? 'rotate-180' : ''
                    ]
                },
                onClick: () => row.toggleExpanded()
            })
        }
    },
    {
        accessorKey: 'date',
        header: 'Date',
        cell: ({ row }) => {
            const date = new Date(row.getValue('date'))
            return h('div', { class: 'font-medium' }, formatDate(date))
        }
    },
    {
        accessorKey: 'description',
        header: 'Description',
        cell: ({ row }) => {
            const recordCount = row.original.transactions.length
            const deductionCount = row.original.deductions.length
            
            return h('div', { class: 'flex flex-col gap-1' }, [
                h('span', { class: 'font-medium' }, row.getValue('description')),
                deductionCount > 0 && h('span', { class: 'text-xs text-gray-500' }, 
                    `${deductionCount} déduction${deductionCount > 1 ? 's' : ''}`)
            ])
        }
    },
    {
        accessorKey: 'status',
        header: 'Statut',
        cell: ({ row }) => {
            const status = row.getValue('status') as string
            const statusConfig = {
                'Pending': { color: 'amber', label: 'En attente' },
                'Completed': { color: 'green', label: 'Validé' },
                'Cancelled': { color: 'red', label: 'Annulé' }
            }
            const config = statusConfig[status as keyof typeof statusConfig] || { color: 'gray', label: status }
            
            return h(UBadge, {
                label: config.label,
                color: config.color,
                variant: 'subtle',
                size: 'sm'
            })
        }
    },
    {
        accessorKey: 'subTotal',
        header: () => h('div', { class: 'text-right' }, 'Sous-total'),
        cell: ({ row }) => {
            const amount = Number.parseFloat(row.getValue('subTotal'))
            const type = row.original.type

            const formatted = new Intl.NumberFormat('fr-CA', {
                style: 'currency',
                currency: 'CAD'
            }).format(amount)

            return h('div', {
                class: 'text-right font-medium',
                style: { color: getTypeColor(row.original.type) }
            }, formatted)
        }
    },
    {
        accessorKey: 'total',
        header: () => h('div', { class: 'text-right font-semibold' }, 'Total'),
        cell: ({ row }) => {
            const amount = Number.parseFloat(row.getValue('total'))
            const type = row.original.type

            const formatted = new Intl.NumberFormat('fr-CA', {
                style: 'currency',
                currency: 'CAD'
            }).format(amount)

            return h('div', {
                class: 'text-right font-semibold text-lg',
                style: { color: getTypeColor(row.original.type) }
            }, formatted)
        }
    },
    {
        id: 'action',
        cell: ({ row }) => {
            return h(
                'div',
                { class: 'text-right' },
                h(
                    UDropdownMenu,
                    {
                        content: { align: 'end' },
                        items: getRowItems(row),
                        'arial-label': 'Actions'
                    },
                    () => h(UButton, {
                        icon: 'i-lucide-ellipsis-vertical',
                        color: 'neutral',
                        variant: 'ghost',
                        'aria-label': 'Actions'
                    })
                )
            )
        }
    }
]

function getRowItems(rows: TableRow<InvoiceTableType>) {
    const options = [
        {
            label: 'Modifier',
            icon: 'i-lucide-pencil',
            onSelect: () => openInvoice(rows.original.id)
        },
        {
            label: 'Supprimer',
            icon: 'i-lucide-trash-2',
            onSelect: () => {
                if (confirm("Voulez-vous supprimer cette transaction ?"))
                    onDelete(rows.original.id)
            }
        }
    ]

    if (rows.original.status === 'Pending') {
        options.splice(1, 0, {
            label: 'Valider',
            icon: 'i-lucide-check',
            onSelect: async () => {
                if (confirm("Voulez-vous confirmer cette transaction ?")) {
                    await useCompleteInvoice(rows.original.id)
                    refresh()
                }
            }
        })
    }

    return options;
}

function getTypeColor(type: string) {
    if (type && type.toLowerCase() === 'income')
        return '#10b981'
    else if (type && type.toLowerCase() === 'other')
        return '#b2bac4'
    else
        return '#ef4444'
}

function getRecordTypeColor(type: string) {
    if (type && type.toLowerCase() === 'credit')
        return '#10b981'
    else
        return '#ef4444'
}

</script>

<template>
    <div class="space-y-6">
        <!-- En-tête avec stats -->
        <div class="bg-gradient-to-br from-primary-50 to-primary-100 dark:from-gray-800 dark:to-gray-900 rounded-xl p-6 shadow-sm">
            <div class="flex justify-between items-start flex-wrap gap-4">
                <div class="space-y-4">
                    <div>
                        <p class="text-sm text-gray-600 dark:text-gray-400 mb-1">Solde actuel</p>
                        <p class="text-4xl font-bold text-gray-900 dark:text-white">
                            {{ formatCurrency(data?.balance ?? 0) }}
                        </p>
                    </div>
                    
                    <div v-if="data" class="flex gap-6">
                        <div>
                            <p class="text-xs text-gray-600 dark:text-gray-400 mb-1">Revenus</p>
                            <p class="text-lg font-semibold text-green-600">
                                {{ formatCurrency(data.income) }}
                            </p>
                        </div>
                        <div>
                            <p class="text-xs text-gray-600 dark:text-gray-400 mb-1">Dépenses</p>
                            <p class="text-lg font-semibold text-red-600">
                                {{ formatCurrency(data.spends) }}
                            </p>
                        </div>
                    </div>
                </div>

                <div class="flex items-center gap-3">
                    <FilterTransactionDrawer @submit="onFilter" />
                    <UButton 
                        icon="i-lucide-plus" 
                        label="Nouvelle transaction" 
                        size="lg"
                        @click="openInvoice()" 
                    />
                </div>
            </div>
        </div>

        <!-- Filtres actifs -->
        <div class="flex flex-wrap gap-3 items-center">
            <USwitch v-model="query.isFreeze" label="Transactions gelées" />
            
            <div v-if="query.accountIds?.length === 0">
                <UBadge label="Tous les comptes" color="gray" variant="subtle" size="lg" />
            </div>
            
            <div v-else class="flex flex-wrap gap-2">
                <UBadge 
                    v-for="account of utils?.accounts.filter(i => query.accountIds?.includes(i.id))"
                    :key="account.id"
                    :label="account.title"
                    color="primary"
                    variant="subtle"
                    size="lg"
                />
            </div>
        </div>

        <!-- Table des transactions -->
        <div v-if="status === 'pending'" class="flex justify-center items-center py-12">
            <div class="flex flex-col items-center gap-3">
                <UIcon name="i-lucide-loader-2" class="w-8 h-8 animate-spin text-primary-500" />
                <p class="text-gray-600 dark:text-gray-400">Chargement...</p>
            </div>
        </div>

        <div v-else-if="status === 'success'" class="bg-white dark:bg-gray-900 rounded-xl shadow-sm border border-gray-200 dark:border-gray-800">
            <UTable 
                :data="data?.transactions"
                v-model:expanded="expandedState"
                :columns="tableColumn"
            >
                <template #expanded="{ row }">
                    <div class="px-4 py-4 bg-gray-50 dark:bg-gray-800/50">
                        <!-- Records -->
                        <div class="space-y-3">
                            <h4 class="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3">
                                Articles ({{ row.original.transactions.length }})
                            </h4>
                            
                            <div 
                                v-for="record in row.original.transactions" 
                                :key="record.id"
                                class="flex items-start gap-4 p-3 bg-white dark:bg-gray-900 rounded-lg border border-gray-200 dark:border-gray-700"
                            >
                                <!-- Icône catégorie -->
                                <div 
                                    class="flex items-center justify-center rounded-full flex-shrink-0"
                                    :style="{
                                        background: `${record.category.color}22`,
                                        width: '40px',
                                        height: '40px',
                                    }"
                                >
                                    <UIcon 
                                        :name="record.category.icon" 
                                        class="text-lg"
                                        :style="{ color: record.category.color }" 
                                    />
                                </div>

                                <!-- Détails -->
                                <div class="flex-1 min-w-0">
                                    <div class="flex justify-between items-start gap-4">
                                        <div class="flex-1 min-w-0">
                                            <p class="font-bold text-gray-900 dark:text-white">
                                                {{ record.category.title }}
                                            </p>
                                            <p class="font-medium text-gray-900 dark:text-white">
                                                {{ record.description }}
                                            </p>
                                        </div>
                                        
                                        <p 
                                            class="font-semibold text-lg whitespace-nowrap"
                                            :style="{ color: getRecordTypeColor(row.original.mouvement) }"
                                        >
                                            {{ formatCurrency(record.amount) }}
                                        </p>
                                    </div>

                                    <!-- Tags et budgets -->
                                    <div class="flex flex-wrap gap-2 mt-3">
                                        <UBadge 
                                            v-for="tag in record.tags" 
                                            :key="tag.id"
                                            :label="tag.value"
                                            :style="{ borderColor: tag.color, color: tag.color }"
                                            variant="outline"
                                            size="sm"
                                        />
                                        <UBadge 
                                            v-for="budget in record.budgets" 
                                            :key="budget.id"
                                            :label="budget.title"
                                            color="primary"
                                            variant="subtle"
                                            size="sm"
                                            icon="i-lucide-pie-chart"
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Déductions -->
                        <div v-if="row.original.deductions.length > 0" class="mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
                            <h4 class="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3">
                                Déductions
                            </h4>
                            
                            <div class="space-y-2">
                                <div 
                                    v-for="(deduction, idx) in row.original.deductions"
                                    :key="idx"
                                    class="flex justify-between items-center p-2 bg-amber-50 dark:bg-amber-900/20 rounded-lg"
                                >
                                    <span class="text-sm font-medium text-gray-700 dark:text-gray-300">
                                        {{ deduction.name }}
                                    </span>
                                    <span class="text-sm font-semibold text-amber-700 dark:text-amber-400">
                                        -{{ formatCurrency(deduction.amount) }}
                                    </span>
                                </div>
                            </div>
                        </div>

                        <!-- Récapitulatif -->
                        <div class="mt-4 pt-4 border-t border-gray-200 dark:border-gray-700 flex justify-end">
                            <div class="space-y-1 min-w-[200px]">
                                <div class="flex justify-between text-sm">
                                    <span class="text-gray-600 dark:text-gray-400">Sous-total:</span>
                                    <span class="font-medium">{{ formatCurrency(row.original.subTotal) }}</span>
                                </div>
                                <div v-if="row.original.deductions.length > 0" class="flex justify-between text-sm">
                                    <span class="text-gray-600 dark:text-gray-400">Déductions:</span>
                                    <span class="font-medium text-amber-600">
                                        -{{ formatCurrency(row.original.deductions.reduce((s, d) => s + d.amount, 0)) }}
                                    </span>
                                </div>
                                <div class="flex justify-between text-base font-bold pt-1 border-t">
                                    <span>Total:</span>
                                    <span :style="{ color: getTypeColor(row.original.type)}">
                                        {{ formatCurrency(row.original.total) }}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </template>
            </UTable>

            <!-- Pagination -->
            <div class="flex items-center justify-between px-4 py-4 border-t border-gray-200 dark:border-gray-800">
                <UPagination 
                    v-model:page="page"
                    @update:page="v => { query.offset = (query.limit * (v - 1)); expandedState = {} }"
                    :items-per-page="query.limit"
                    :total="data?.total"
                    active-variant="subtle"
                />
                
                <div class="flex items-center gap-2">
                    <span class="text-sm text-gray-600 dark:text-gray-400">Lignes par page:</span>
                    <UInputNumber 
                        v-model="query.limit"
                        :min="1"
                        :max="50"
                        orientation="vertical"
                        size="sm"
                        class="w-20"
                    />
                </div>
            </div>
        </div>
    </div>
</template>