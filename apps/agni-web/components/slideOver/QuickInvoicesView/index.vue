<script setup lang="ts">
import type { AccountWithDetailType, SlideQuickViewTransactionType } from '~/types/ui/account';
import ListTransaction from './ListTransaction.vue';
import type { QueryFilterRequest } from '~/types/api';
import type { GetBalanceResponse, QueryInvoice } from '~/types/api/transaction';
import { fetchBalance, fetchInvoice, fetchInvoicePagination, useDeleteInvoice, useFreezeInvoice, useTransfertInvoice } from '~/composables/api/invoices.js';
import { useInfiniteScroll } from '@vueuse/core';
import { ModalEditFreezeInvoice, ModalEditTransfer, ModalInvoice } from '#components';
import type { EditFreezeInvoiceType, EditTransfertType, InvoiceType } from '~/types/ui/transaction.js';
import { getLocalTimeZone } from '@internationalized/date';


function formatInvoiceToSlideItem(invoice: InvoiceType): SlideQuickViewTransactionType {
    return {
        id: invoice.id,
        date: invoice.date,
        status: invoice.status,
        category: invoice.transactions.length > 0 ? invoice.transactions[0]!.category.title : '',
        color: invoice.transactions.length > 0 ? invoice.transactions[0]!.category.color : '',
        icon: invoice.transactions.length > 0 ? invoice.transactions[0]!.category.icon : '', 
        type: invoice.type,
        subTotal: invoice.subTotal,
        description: invoice.transactions.length > 0 ? invoice.transactions[0]!.description : '',
        total: invoice.total
    } 
}

const { account, budgetIds, tagIds, categoryIds } = defineProps<{
    account?: AccountWithDetailType,
    budgetIds?: string[],
    tagIds?: string[]
    categoryIds?: string[]
}>();
const emit = defineEmits<{
    close: [refresh: boolean]
}>();

const toast = useToast()
const el = useTemplateRef('el')
const doRefresh = ref(false)

const queryAllTrans = reactive<QueryFilterRequest & QueryInvoice>({
    offset: 0,
    limit: 10,
    accountIds: account ? [account.id] : [],
    categoryIds: categoryIds || [],
    tagIds: tagIds || [],
    budgetIds: budgetIds || [],
    isFreeze: false
});
const balance = ref<GetBalanceResponse>()
const loading = ref(false);
const totalInvoices = ref(0)
const invoices = ref<SlideQuickViewTransactionType[]>([])
const hasMore = computed(() => invoices.value.length < totalInvoices.value)
const showFreeze = ref(false)

const overlay = useOverlay()
const modalTransfer = overlay.create(ModalEditTransfer);
const modalInvoice = overlay.create(ModalInvoice);
const modalFreezeInvoice = overlay.create(ModalEditFreezeInvoice);

async function getAllInvoices(offset: number = 0) {
    if (loading.value) return
    loading.value = true
    try {
        queryAllTrans.offset = offset

        const transactions = await fetchInvoicePagination(queryAllTrans);
        const res = await fetchBalance(queryAllTrans);

        const resInvoices = transactions.items.map(i => formatInvoiceToSlideItem(i))

        invoices.value.push(...resInvoices)

        balance.value = res
        totalInvoices.value = transactions.total

    } catch(err) {
        console.error(err)
    } finally {
        loading.value = false
    } 
}

async function resetAllInvoices() {
    queryAllTrans.limit = invoices.value.length
    invoices.value = []
    balance.value = undefined
    await getAllInvoices(0)
    queryAllTrans.limit = 10
}

async function openModalEditInvoice(invoiceId?:string) {
    let invoice;
    if (invoiceId)
        invoice = await fetchInvoice(invoiceId)

    const instance = modalInvoice.open({
        accountSelectedId: account?.id,
        invoice: invoice
    })

    await instance.result 

    resetAllInvoices()
    doRefresh.value = true
} 

async function onTransfertAccount(value: EditTransfertType) {
    try {
        await useTransfertInvoice({
            accountIdFrom: value.accountIdFrom,
            accountIdTo: value.accountIdTo,
            amount: value.amount,
            date: value.date.toDate(getLocalTimeZone()).toISOString()
        })

        resetAllInvoices()
        doRefresh.value = true
    } catch(err) {
        toast.add({
            title: 'Error tranfert',
            description: 'Error while transfert account',
            color: 'error'
        });
    }
} 

async function openModalTransferAccount (){ 
    modalTransfer.open({
        accountId: account?.id,
        onSubmit: onTransfertAccount 
    });
}

async function onFreezeInvoice(value: EditFreezeInvoiceType) {
    try {
        await useFreezeInvoice({
            accountId: value.accountId,
            title: value.title,
            amount: value.amount,
            endDate: value.endDate.toDate(getLocalTimeZone()).toISOString(),
            status: value.status
        })
        resetAllInvoices()
        doRefresh.value = true
    } catch(err) {
        toast.add({
            title: 'Error Freeze',
            description: 'Error while freeze account',
            color: 'error'
        });
    }
}

async function openModalEditFreeze() {
    const instance = modalFreezeInvoice.open({
        accountId: account?.id,
        onSubmit: onFreezeInvoice
    });

    await instance.result
}

async function deleteInvoice(invoiceId: string) {
    try {
        const ok = confirm("Voulez-vous supprimer cette facture ?")
        if (ok) {
            await useDeleteInvoice(invoiceId)

            resetAllInvoices()
            doRefresh.value = true
        }
    } catch (err) {
        toast.add({
            title: 'Error Freeze',
            description: 'Error while freeze account',
            color: 'error'
        });
    }
    
}

watch(showFreeze, (val) => {
    queryAllTrans.isFreeze = val
    invoices.value = []
    totalInvoices.value = 0
    balance.value = undefined
    el.value?.scrollTo({ top: 0 })
    getAllInvoices(0)
}, { immediate: true })

useInfiniteScroll(
    el,
    () => {
        if (!loading.value && hasMore.value)
            getAllInvoices(invoices.value.length)
    }, {
        distance: 10,
        canLoadMore() {
            return hasMore.value
        },
    }
)

</script>

<template>
    <USlideover :close="{ onClick: () => emit('close', doRefresh) }">
        <template #content>
            <div ref="el" class="space-y-6 p-6 overflow-auto">
                <div class="flex justify-end">
                    <UButton 
                        icon="i-lucide-x"
                        variant="ghost"
                        @click="emit('close', doRefresh)"
                    />
                </div>
                <SlideOverQuickInvoicesViewHeader 
                    :account-info="account"
                    :gains="balance?.income ?? 0"
                    :spend="balance?.spend ?? 0"
                    @create-invoice="() => openModalEditInvoice()"
                    @transfer="() => openModalTransferAccount()"
                    @freeze="() => openModalEditFreeze()"
                />

                <div class="flex items-center gap-1 w-fit rounded-xl bg-neutral-100 dark:bg-neutral-800 p-1">
                    <button
                        @click="showFreeze = false"
                        :class="[
                            'px-3 py-1.5 text-sm font-medium rounded-lg transition-colors',
                            !showFreeze
                                ? 'bg-white dark:bg-neutral-700 shadow-sm text-neutral-900 dark:text-white'
                                : 'text-neutral-500 hover:text-neutral-700 dark:text-neutral-400'
                        ]"
                    >
                        Toutes
                    </button>
                    <button
                        @click="showFreeze = true"
                        :class="[
                            'px-3 py-1.5 text-sm font-medium rounded-lg transition-colors',
                            showFreeze
                                ? 'bg-white dark:bg-neutral-700 shadow-sm text-neutral-900 dark:text-white'
                                : 'text-neutral-500 hover:text-neutral-700 dark:text-neutral-400'
                        ]"
                    >
                        Gelées
                    </button>
                </div>

                <LoadingIndicator v-if="loading && invoices.length === 0" />

                <div v-else-if="invoices.length > 0">
                    <ListTransaction 
                        :invoices="invoices" 
                        :loading="loading" 
                        :has-more="hasMore"  
                        @update="id => openModalEditInvoice(id)"
                        @delete="id => deleteInvoice(id)"
                    />
                </div>

                <div v-else 
                    class="text-center py-12">
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