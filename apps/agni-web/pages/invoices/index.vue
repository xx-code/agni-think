<script setup lang="ts">
import type { DropdownMenuItem, TableColumn, TableRow } from "@nuxt/ui";
import type { InvoiceFilter, InvoiceTableType, TransactionTableType } from "~/types/ui/transaction";
import type { FormFilterTransaction } from "~/types/ui/component";
import { getLocalTimeZone } from "@internationalized/date";
import type { QueryFilterRequest } from "~/types/api";
import { ModalInvoice } from "#components";
import { listAccountsToListAccount } from "~/mappers/account";
import { budgetFilterToBudgetQueryRequest, listBudgetsResponseToListBudgets } from "~/mappers/budget";
import { listCategoriesResponseToListCategories } from "~/mappers/category";
import { listDeductionsResponseToListDeductions } from "~/mappers/deduction";
import { invoiceResponseToInvoice, listInvoicesResponseToListInvoices } from "~/mappers/invoice";
import { listTagsResponseToListTags } from "~/mappers/tag";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";
import { getApiAgent } from "~/utils/env";
import type { GetAccountResponse } from "~/types/api/account";
import type { GetCategoryResponse } from "~/types/api/category";
import type { GetDeductionResponse } from "~/types/api/deduction";
import type { GetBalanceResponse, GetInvoiceResponse } from "~/types/api/transaction";
import type { GetTagResponse } from "~/types/api/tag";
import type { ListResponse } from "~/types/api";

const { start, stop } = useLoading()
const isLoadingTransactions = ref(false)
const isLoadingRefs = ref(false)
const page = ref(1);

const initQueryFilter = (): InvoiceFilter => {
    return {
        offset: 0,
        limit: 8,
        queryAll: false,
        accountIds: [],
        categoryIds: [],
        tagIds: [],
        budgetIds: [],
        isFreeze: false
    } 
}
const query = reactive<InvoiceFilter>(initQueryFilter());

const hasFilters = computed(() => {
    const ignoredKeys = new Set(['offset', 'limit', 'queryAll'])

    return Object.entries(query).some(([key, value]) => {
        if (ignoredKeys.has(key)) return false

        if (Array.isArray(value)) return value.length > 0
        if (typeof value === 'boolean') return value === true
        if (typeof value === 'string') return value.trim().length > 0
        if (typeof value === 'number') return value !== 0

        return value !== undefined && value !== null
    })
})

const { data: utils } = useAsyncData('utils-transactions', async () => {
    isLoadingRefs.value = true
    const [ accounts, categories, deductions, budgets, tags] = await Promise.all([
        ApiLinkBuilder.route<ListResponse<GetAccountResponse>>(API_ROUTES.ACCOUNTS.GET_ACCOUNTS).query({ offset: 0, limit: 0, queryAll: true }).mapper(listAccountsToListAccount).execute(),
        ApiLinkBuilder.route<ListResponse<GetCategoryResponse>>(API_ROUTES.CATEGORIES.GET_CATEGORIES).query({ offset: 0, limit: 0, queryAll: true }).mapper(listCategoriesResponseToListCategories).execute(),
        ApiLinkBuilder.route<ListResponse<GetDeductionResponse>>(API_ROUTES.DEDUCTIONS.GET_DEDUCTIONS).query({ offset: 0, limit: 0, queryAll: true }).mapper(listDeductionsResponseToListDeductions).execute(),
        ApiLinkBuilder.route(API_ROUTES.BUDGETS.GET_BUDGETS).query(budgetFilterToBudgetQueryRequest({ offset: 0, limit: 0, queryAll: true })).mapper(listBudgetsResponseToListBudgets).execute(),
        ApiLinkBuilder.route<ListResponse<GetTagResponse>>(API_ROUTES.TAGS.GET_TAGS).query({ offset: 0, limit: 0, queryAll: true }).mapper(listTagsResponseToListTags).execute()
    ])
    isLoadingRefs.value = false

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

const { data, refresh } = useAsyncData(`transactions-${JSON.stringify(query)}`, async () => {
    isLoadingTransactions.value = true
    const [transactions, balance ] = await Promise.all([
        ApiLinkBuilder.route<ListResponse<GetInvoiceResponse>>(API_ROUTES.INVOICES.GET_INVOICES).query(query).mapper(listInvoicesResponseToListInvoices).execute(),
        ApiLinkBuilder.route<GetBalanceResponse>(API_ROUTES.INVOICES.GET_BALANCES).query(query).execute(),
    ])
    isLoadingTransactions.value = false

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
                category: record.category,
                tags: record.tags,
                budgets: record.budgets
            } satisfies TransactionTableType)),
            deductions: i.deductions.map(d => ({
                name: getDeduction(d.id)?.description || '', // ou autre nom si disponible
                amount: d.amount 
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
const modalInvoice = overlay.create(ModalInvoice);

async function openInvoice(transactionId?: string) {
    let invoice: any | undefined;
    if (transactionId)
        invoice = await ApiLinkBuilder.route<GetInvoiceResponse>(API_ROUTES.INVOICES.GET_INVOICE).params({id: transactionId}).mapper(invoiceResponseToInvoice).execute();

    const instance = modalInvoice.open({
        invoice: invoice,
    });

    await instance.result

    refresh()
};

async function syncBank() {
    try {
        start()
        await ApiLinkBuilder
            .route(API_ROUTES.BANK.SYNC_TRANSACTION)
            .execute()
        query.status = "Pending"
        query.offset = 0
        page.value = 1
    } catch(err) {
        stop()
        console.log(err)
        alert(err)
    } finally {
        stop()
    }
}

const textTransaction = ref("") 
const openScanTransaction = ref(false)
async function scanNewTransaction() {
    if (textTransaction.value.trim() !== "") {
        try {
            start()
            await $fetch<string>(`${getApiAgent()}/treat-unformat-transaction`, { method: 'POST', body: { text: textTransaction.value } })
            query.status = "Pending"
            query.offset = 0
            page.value = 1
        } catch(err) {
            stop()
            console.log(err)
            alert(err)
        } finally {
            openScanTransaction.value = false
            stop()
        }
    } 
}


const onDelete = async (id: string) => {
    await ApiLinkBuilder.route(API_ROUTES.INVOICES.DELETE_INVOICE).params({id}).execute()
    refresh()
}

function onFilter() {
    query.offset = 0
    page.value = 1
    
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
                    await ApiLinkBuilder.route(API_ROUTES.INVOICES.COMPLETE_INVOICE).params({id: rows.original.id}).execute()
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

function cleanQueryFilter(){
    Object.assign(query, initQueryFilter())
}
</script>

<template>
    <UiPage>
        <UiPageHeader 
            title="Mes Factures"
            :button="
                {
                    icon: 'i-lucide-plus',
                    label: 'Ajouter une facture'
                }
            "
            subtitle="Gérez vos factures et consulter les"
            @click-button="openInvoice()"
        />

        <div class="grid grid-cols-2 lg:grid-cols-3 gap-4">
            <UiBannerAccountant 
                title="Solde actuel"
                :amount="data?.balance ?? 0"
                :icon="{ name: 'i-lucide-scale', backgroundColor: 'rgba(59, 130, 246, 0.1)', fontColor: '#3b82f6' }"
            />

            <UiBannerAccountant 
                title="Revenus"
                :amount="data?.income ?? 0"
                :icon="{ name: 'i-lucide-trending-up', backgroundColor: 'rgba(16, 185, 129, 0.1)', fontColor: '#10b981' }"
            />

            <UiBannerAccountant 
                title="Dépenses"
                :amount="data?.spends ?? 0"
                :icon="{ name: 'i-lucide-trending-down', backgroundColor: 'rgba(239, 68, 68, 0.1)', fontColor: '#ef4444' }"
            />
        </div>

        <UiInvoiceHeader 
            :accounts="utils?.accounts.map(i => ({value: i.id, label: i.title})) ?? []"
            :categories="utils?.categories.map(i => ({value: i.id, label: i.title})) ?? []"
            v-model="query"
            @transfer=""
            @freeze=""
            @filter="() => { 
                page = 1
                console.log(query)
            }"
            @scan-invoice="openScanTransaction = true"
            @sync-bank="syncBank()"
        />

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
        <div v-if="isLoadingTransactions" class="flex justify-center items-center py-12">
            <div class="flex flex-col items-center gap-3">
                <UIcon name="i-lucide-loader-2" class="w-8 h-8 animate-spin text-primary-500" />
                <p class="text-gray-600 dark:text-gray-400">Chargement...</p>
            </div>
        </div>

        <div v-else-if="!isLoadingTransactions" class="bg-white dark:bg-gray-900 rounded-xl shadow-sm border border-gray-200 dark:border-gray-800">
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
    </UiPage>
</template>