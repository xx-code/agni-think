<script setup lang="ts">
import {  h,  ref, resolveComponent  } from "vue"; 
import type { TableColumn, TableRow } from "@nuxt/ui"; 
import { fetchAccounts } from "~/composables/accounts/useAccounts";
import { fetchBudgets } from "~/composables/budgets/useBudgets";
import  { fetchCategories } from "~/composables/categories/useCategories";
import { fetchTags } from "~/composables/tags/useTags";
import { fetchTransactionPagination } from "~/composables/transactions/useTransactionPagination";
import { fetchBalance } from "~/composables/transactions/useBalance";
import useDeleteTransaction from "~/composables/transactions/useDeleteTransaction";
import type { EditTransactionType, TransactionTableType, TransactionType } from "~/types/ui/transaction";
import type { FilterTransactionQuery} from "~/types/api/transaction";
import { ModalEditTransaction } from "#components";
import { fetchTransaction } from "~/composables/transactions/useTransaction";
import useUpdateTransaction from "~/composables/transactions/useUpdateTransaction";
import useCreateTransaction from "~/composables/transactions/useCreateTransaction";
import type { FormFilterTransaction } from "~/types/ui/component";
import useCompleteTransaction from "~/composables/transactions/useCompleteTransaction";
import { getLocalTimeZone } from "@internationalized/date";
import { fetchTag } from "~/composables/tags/useTag";


const toast = useToast();

// const paramsTransactions = ref<FilterTransactionQuery>({
//     offset: 
// });

const page = ref(1);
const query = reactive<FilterTransactionQuery>({
    offset: 0,
    limit: 8,
    accountFilterIds: [],
    categoryFilterIds: [],
    tagFilterIds: [],
    budgetFilterIds: [],
    minPrice: undefined,
    maxPrice: undefined,
    dateStart: undefined,
    dateEnd: undefined,
    isFreeze: false
});

const {data, error, refresh, status } = useAsyncData('all-transactions', async () => {
    const transactions = await fetchTransactionPagination(query)
    const balance = await fetchBalance(query)

    const [accounts, categories, budgets, tags] = await Promise.all([ fetchAccounts({offset: 0, limit: 0, queryAll: true}), 
     fetchCategories({offset: 0, limit: 0, queryAll: true}), fetchBudgets({offset: 0, limit: 0, queryAll: true}), 
     fetchTags({offset: 0, limit: 0, queryAll: true})])

    const getCategory = (id: string) => categories.items.find(i => id === i.id)
    const getTag = (id: string) => tags.items.find(i => id === i.id)
    const getBudget = (id: string) => budgets.items.find(i => id === i.id)

    return {
        accounts: accounts.items,
        transactions: transactions.items.map(i => ({
            id: i.id,
            accountId: i.accountId,
            amount: i.amount,
            date: i.date,
            description: i.description,
            recordType: i.recordType,
            type: i.type,
            status: i.status,
            category: {
                id: i.categoryId,
                icon: getCategory(i.categoryId)?.icon || '',
                color: getCategory(i.categoryId)?.color || '',
                title: getCategory(i.categoryId)?.title || '',
            },
            tags: i.tagIds.map(i => ({
                id: i,
                value: getTag(i)?.value || '',
                color: getTag(i)?.color || ''
            })),
            budgets: i.budgetIds.map(i => ({
                id: i,
                title: getBudget(i)?.title || ''
            }))
        } satisfies TransactionTableType)),
        total: transactions.totals, 
        balance: balance.balance 
    }
}, { watch: [query]});

const extentedState = ref()

const overlay = useOverlay()
const modalTransaction = overlay.create(ModalEditTransaction);

async function onSubmitTransaction(value: EditTransactionType, oldValue?: TransactionType) {
    try {
        if (oldValue)
            await useUpdateTransaction(oldValue.id, {
                accountId: value.accountId,
                amount: value.amount,
                budgetIds: value.budgetIds,
                categoryId: value.categoryId,
                date: value.date.toDate(getLocalTimeZone()).toISOString(),
                description: value.description,
                tagIds: value.tagIds,
                type: value.type
            });
        else  {
            await useCreateTransaction({
                accountId: value.accountId,
                amount: value.amount,
                budgetIds: value.budgetIds,
                categoryId: value.categoryId,
                date: value.date.toDate(getLocalTimeZone()).toISOString(),
                description: value.description,
                tagIds: value.tagIds,
                type: value.type
            });
        }
        refresh()
    } catch(err) {
        toast.add({
            title: 'Error submit transaction',
            description: 'Error while submit transaction ' + err,
            color: 'error'
        })
    }
}

async function openTransaction(transactionId?: string) {
    let transaction:TransactionType|undefined;
    if (transactionId)
        transaction = await fetchTransaction(transactionId);

    modalTransaction.open({
        transaction: transaction,
        onSubmit: onSubmitTransaction 
    }); 
};

const onDelete = async (id: string) => {
    await useDeleteTransaction(id)
    refresh()
}

function onFilter(value: FormFilterTransaction) {
    query.categoryFilterIds = value.categoryIds
    query.tagFilterIds = value.tagIds
    query.accountFilterIds = value.accountIds
    query.budgetFilterIds = value.budgetIds
    query.dateEnd = value.dateEnd ? new Date(value.dateEnd) : undefined
    query.dateStart = value.dateStart ? new Date(value.dateStart) : undefined
    query.minPrice = value.minPrice
    query.types = value.types
    query.maxPrice = value.minPrice
    query.status = value.status
    query.offset = 0
    page.value = 1

    extentedState.value = {}
}


const UIcon = resolveComponent('UIcon');
const UButton = resolveComponent('UButton');
const UDropdownMenu = resolveComponent('UDropdownMenu');
const UBadge = resolveComponent('UBadge');
const tableColumn: TableColumn<TransactionTableType>[] = [
    {
        id: 'expand',
        cell: ({ row }) => {
            const tags = row.original.tags
            if (tags.length > 0)
                return (
                    h(UButton, {
                        color: 'neutral',
                        variant: 'ghost',
                        icon: 'i-lucide-chevron-down',
                        square: true,
                        'aria-label': 'Expand',
                        ui: {
                            leadingIcon: [
                                'transition-transform',
                                row.getIsExpanded() ? 'duration-200 rotate-180': ''
                            ]
                        },
                        onClick: () => row.toggleExpanded()
                    })
                )
        }
    },
    {
        id: 'icon',
        header: '',
        cell: ({ row }) => {
            const icon = row.original.category.icon
            const color = row.original.category.color
            return(
                h('div', {
                    class: 'flex items-center justify-center rounded-full',
                    style: {
                        background: `${color}22`,
                 
                        width: '35px',
                        height: '35px',
                    }
                }, [
                    h(UIcon, { name: icon, class: 'text-white text-lg', style:{color: color}  }) // ou une autre couleur si tu veux
                ])
            )
        }
    },
    {
        accessorKey: 'date',
        header: 'Date',
        cell: ({ row }) => {
            return formatDate(row.getValue('date')) 
        }
    },
    {
        accessorKey: 'category.title',
        header: 'Categorie',
    },
    {
        accessorKey: 'description',
        header: 'Desription',
    },
    {
        accessorKey: 'status',
        header: 'Status'   
    },
    {
        accessorKey: 'amount',
        header: () => h('div', { class: 'text-right' }, 'Amount'),
        cell: ({ row }) => {
            const amount = Number.parseFloat(row.getValue('amount'))
            const recordType = row.original.recordType
            

            const formatted = new Intl.NumberFormat('en-US', {
                style: 'currency',
                currency: 'CAD'
            }).format(amount)

            return h('div', {class: 'text-right font-medium', style: {color: recordType === 'Debit' ? "" : "#1abc9c"}}, formatted)
            
        }
    },
    {
        id: 'action',
        cell: ({ row }) => {
            return h(
                'div',
                {  class: 'text-right'},
                h(
                    UDropdownMenu,
                    {
                        content: {
                            align: 'end'
                        },
                        items: getRowItems(row),
                        'arial-label': 'Actions dropdown'
                    },
                    () => 
                        h(
                            UButton, {
                                icon: 'i-lucide-ellipsis-vertical',
                                color: 'neutral',
                                variant: 'ghost',
                                class: 'ml-auto',
                                'aria-label': 'Actions dropdown'
                            }
                        )
                )
            )
        }
    }
]
function getRowItems(rows: TableRow<TransactionTableType>) {
    const options = [
        {
            label: 'Modifier',
            onSelect: () => {
                const id = rows.original.id
                openTransaction(id)
            }
        },
        {
            label: 'Supprimer',
            onSelect: () => {
                const id = rows.original.id
                if (confirm("Voulez vous supprimer la transaction"))
                    onDelete(id)
            }
        }
    ] 

    if (rows.original.status === 'Pending'){
        options.push({
            label: 'Valider',
            onSelect: async () => {
                if (confirm("Voulez confirmer la transaction")) {
                    await useCompleteTransaction(rows.original.id) 
                    refresh()
                }
            } 
        })

        // change position
        const lastOption = options[1];
        options[1] = options[2];
        options[2] = lastOption; 
    }
    
    return options;
}


</script>

<template>
    <div>
        <div class="flex justify-between flex-wrap" style="margin-top: 1rem;">
            <div class="flex items-center gap-3">
                <UButton variant="outline" color="neutral" :label="formatCurrency(data ? data.balance : 0)" size="xl"/>
                <FilterTransactionDrawer @submit="onFilter" /> 

                <USwitch v-model="query.isFreeze"  label="Freeze Transactions"/>
            </div>

            <UButton icon="i-lucide-plus" label="Ajouter transaction" size="xl" @click="openTransaction()" />
        </div>

        <div class="mt-2">
            <div v-if="query.accountFilterIds?.length === 0">
                <UButton color="neutral" variant="outline" label="Tous les comptes"/>
            </div>
            <div class="flex flex-wrap gap-1" 
                v-if="query.accountFilterIds && query.accountFilterIds.length > 0">
                <div v-for="account of  data?.accounts.filter(i => query.accountFilterIds?.includes(i.id))" 
                    :key="account.id">
                    <UButton color="neutral" variant="outline" :label="account.title"/>
                </div>
            </div>
        </div> 

        <div v-if="status === 'pending'" style="margin-top: 1rem;">
            loading...
        </div>

        <div  v-if="status === 'success'" style="margin-top: 1rem;" class="bg-white p-2 rounded-lg">
            <UTable 
                :data="data?.transactions" 
                ref="table"
                v-model:expanded="extentedState"
                :columns="tableColumn" 
                class="flex-1">
                <template #expanded="{ row }">
                    <div class="flex flex-row flex-wrap gap-2">
                        <div v-for="tag in row.original.tags" :key="tag.id">
                            <UBadge :label="tag.value" :style="{color:tag.color, borderColor: tag.color}" variant="outline" color="neutral"/>  
                        </div>
                        <div v-for="budget in row.original.budgets" :key="budget.id" >
                            <UBadge :label="budget.title" variant="outline" color="neutral"/>  
                        </div>
                    </div>
                </template>
            </UTable>
            <div class="flex flex-row gap-2 items-baseline-last justify-between">
                <UPagination 
                    class="mt-3" 
                    v-model:page="page" 
                    v-on:update:page="v => { query.offset = (query.limit * (v - 1)); extentedState = {}} "
                    :items-per-page="query.limit"  
                    :total="data?.total" 
                    active-variant="subtle" />
                <UInputNumber 
                    v-model="query.limit" 
                    :min="1" 
                    orientation="vertical" 
                    style="width: 80px;"
                />
            </div>
        </div>
    </div>
</template>

<style scoped>
.icon {
    background: #1616168c;
    color: #161616;
    border-radius: 100%;
    width: 35px;
    height: 35px;
    display: flex;
    justify-content: center;
    align-items: center;
}
</style>