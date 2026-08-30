<script setup lang="ts">
import type { DropdownMenuItem, TableColumn, TableRow } from "@nuxt/ui";
import type { InvoiceFilter, InvoiceTableType, InvoiceType, TransactionTableType } from "~/types/ui/transaction";
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
import useLazyInifinteScroll from "~/composables/ui/useLazyInfiniteScroll";
import { useInfiniteScroll } from "@vueuse/core";
import useConfirmModal from "~/composables/modal/useConfirmModal";

const MAX_ITEMS_TO_DISPLAY=250

const { start, stop } = useLoading()
const isLoadingBalance = ref(false)
const isLoadingRefs = ref(false)
const el = useTemplateRef('el')
const toast = useToast()

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

const { data, query, loading, hasMore, totalData, loadData, reset, updateData, removeData  } = useLazyInifinteScroll<InvoiceFilter, GetInvoiceResponse, InvoiceType>(
    API_ROUTES.INVOICES.GET_INVOICES, 
    API_ROUTES.INVOICES.GET_INVOICE, 
    listInvoicesResponseToListInvoices, 
    invoiceResponseToInvoice,
    initQueryFilter())


const { data:invoiceBalance, refresh } = useAsyncData(`transactions-${JSON.stringify(query)}`, async () => {
    isLoadingBalance.value = true
    const balance = await ApiLinkBuilder.route<GetBalanceResponse>(API_ROUTES.INVOICES.GET_BALANCES).query(query).execute()
    isLoadingBalance.value = false

    return balance
}, { watch: [query] });

const expandedState = ref<Record<string, boolean>>({})

const overlay = useOverlay()
const modalInvoice = overlay.create(ModalInvoice);
const { open: openConfirmDialog } = useConfirmModal(overlay)

const progressPercent = computed(() => {
    if (totalData.value === 0) return 0
    return Math.min(100, Math.round((data.value.length / totalData.value) * 100))
})

async function openInvoice(transactionId?: string) {
    let invoice: any | undefined;
    if (transactionId)
        invoice = await ApiLinkBuilder.route<GetInvoiceResponse>(API_ROUTES.INVOICES.GET_INVOICE).params({id: transactionId}).mapper(invoiceResponseToInvoice).execute();

    modalInvoice.open({
        invoice: invoice,
        onClose: (doRefresh) => {
            if (doRefresh) {
                refresh()
                if (transactionId) {
                    const index = data.value.findIndex(i => i.id === transactionId)
                    if (index >= 0) updateData(index, transactionId)
                } else {
                    reset()
                    loadData()
                } 
            }
            
        }
    })
};

async function syncBank() {
    try {
        start()
        await ApiLinkBuilder
            .route(API_ROUTES.BANK.SYNC_TRANSACTION)
            .execute()
        query.status = "Pending"
        query.offset = 0
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
    openConfirmDialog(
        {
            title: "Voulez vous supprimer la facture?",
            description: ""
        },
        async () => {
            const index = data.value.findIndex(i => i.id === id)
            if (index >= 0) {
                await ApiLinkBuilder.route(API_ROUTES.INVOICES.DELETE_INVOICE).params({id}).execute()
                removeData(index)
                refresh()
            }
        }
    )
}

async function valid(id: string) {
    start()
    try {
        const index = data.value.findIndex(i => i.id === id)
        if (index >= 0) {
            await ApiLinkBuilder.route(API_ROUTES.INVOICES.COMPLETE_INVOICE).params({id}).execute()
            updateData(index, id)
        } 
    } catch(err: any) {
        toast.add({
            title: 'Error Freeze',
            description: err?.message,
            color: 'error'
        });
    } finally {
        stop()
    }
}

useInfiniteScroll(
    el,
    () => {
        if (!loading.value && hasMore.value && data.value.length < MAX_ITEMS_TO_DISPLAY ) {
            query.offset = data.value.length
        }
    }, {
        distance: 5,
        canLoadMore() {
            return hasMore.value
        },
    }
)


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
                :amount="invoiceBalance?.balance ?? 0"
                :icon="{ name: 'i-lucide-scale', backgroundColor: 'rgba(59, 130, 246, 0.1)', fontColor: '#3b82f6' }"
            />

            <UiBannerAccountant 
                title="Revenus"
                :amount="invoiceBalance?.income ?? 0"
                :icon="{ name: 'i-lucide-trending-up', backgroundColor: 'rgba(16, 185, 129, 0.1)', fontColor: '#10b981' }"
            />

            <UiBannerAccountant 
                title="Dépenses"
                :amount="invoiceBalance?.spend ?? 0"
                :icon="{ name: 'i-lucide-trending-down', backgroundColor: 'rgba(239, 68, 68, 0.1)', fontColor: '#ef4444' }"
            />
        </div>

        <UiInvoiceHeader 
            :accounts="utils?.accounts.map(i => ({value: i.id, label: i.title})) ?? []"
            :categories="utils?.categories.map(i => ({value: i.id, label: i.title})) ?? []"
            v-model="query"
            @transfer=""
            @freeze=""
            @filter="(filter) => { 
                reset()
                Object.assign(query, {
                    ...query, 
                    offset: 0,
                    accountIds: filter.accountIds,
                    tagIds: filter.tagIds,
                    budgetIds: filter.budgetIds,
                    categoryIds: filter.categoryIds,
                    endDate: filter.endDate,
                    startDate: filter.startDate,
                    maxAmount: filter.maxAmount,
                    minAmount: filter.minAmount,
                    mouvement: filter.mouvement,
                    status: filter.status,
                    types: filter.types 
                })
            }"
            @scan-invoice="openScanTransaction = true"
            @sync-bank="syncBank()"
        />

        <!-- Filtres actifs -->
        <div class="flex flex-wrap gap-3 items-center">
            <USwitch 
                v-bind:model-value="query.isFreeze"
                label="Transactions gelées" 
                v-on:update:model-value="(v) => {
                    reset()
                    query.isFreeze = v
                }"
            />
        </div>

        <div class="flex flex-col gap-4" >
            <UiInvoiceCard 
                v-for="invoice in data"
                :key="invoice.id"
                :data="invoice"
                :deductions="utils?.deductions ?? []"
                @delete="id => {
                    onDelete(id)
                }"
                @update="id => {
                    openInvoice(id)
                }"
                @valide="id => {
                   valid(id) 
                }"
            />

             <div ref="el" class="py-6">
                <!-- Limite d'affichage atteinte, mais il reste des articles au-delà -->
                <div
                    v-if="hasMore && data.length >= MAX_ITEMS_TO_DISPLAY"
                    class="flex flex-col items-center gap-3 max-w-sm mx-auto"
                >
                    <div class="w-full">
                        <div class="flex justify-between text-xs text-neutral-400 mb-1.5">
                            <span>{{ data.length }} sur {{ totalData }} articles</span>
                            <span>{{ progressPercent }}%</span>
                        </div>
                        <div class="h-1.5 w-full rounded-full bg-neutral-100 dark:bg-neutral-800 overflow-hidden">
                            <div
                                class="h-full rounded-full bg-primary-500 transition-all duration-300"
                                :style="{ width: `${progressPercent}%` }"
                            />
                        </div>
                    </div>
        
                    <p class="text-sm text-neutral-400 text-center">
                        Affichage limité à {{ MAX_ITEMS_TO_DISPLAY }} articles à la fois.
                        Continuez ou affinez avec un filtre pour aller plus vite.
                    </p>
        
                    <UButton
                        label="Afficher plus"
                        variant="outline"
                        color="neutral"
                        icon="i-lucide-chevron-down"
                        :loading="loading"
                        :disabled="loading"
                        @click="() => { query.offset = data.length }"
                    />
                </div>
        
                <!-- Fin de liste -->
                <div v-else-if="!hasMore" class="flex flex-col items-center gap-1.5">
                    <UIcon name="i-lucide-check-circle-2" class="text-lg text-emerald-500" />
                    <p class="text-sm text-neutral-400 text-center">
                        Vous avez atteint la fin de la liste 
                        <span class="font-semibold text-neutral-600 dark:text-neutral-300">{{ totalData }} articles</span>
                        au total.
                    </p>
                </div>
            </div>
        </div>

        <div  v-if="loading" class="flex justify-center items-center py-12">
            <div class="flex flex-col items-center gap-3">
                <UIcon name="i-lucide-loader-2" class="w-8 h-8 animate-spin text-primary-500" />
                <p class="text-gray-600 dark:text-gray-400">Chargement...</p>
            </div>
        </div>
    </UiPage>
</template>