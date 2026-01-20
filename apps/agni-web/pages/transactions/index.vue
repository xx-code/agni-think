<script setup lang="ts">
import { computed, h,  ref, resolveComponent  } from "vue"; 
import type { TableColumn, TableRow } from "@nuxt/ui"; 
import useAccounts from "~/composables/accounts/useAccounts";
import useBudgets from "~/composables/budgets/useBudgets";
import useCategories from "~/composables/categories/useCategories";
import useTags from "~/composables/tags/useTags";
import useTransactionPagination from "~/composables/transactions/useTransactionPagination";
import useBalance from "~/composables/transactions/useBalance";
import useDeleteTransaction from "~/composables/transactions/useDeleteTransaction";
import type { EditTransactionType, TransactionTableType, TransactionType } from "~/types/ui/transaction";
import type { FilterBalanceTransactionQuery, FilterTransactionQuery} from "~/types/api/transaction";
import { ModalEditTransaction } from "#components";
import { fetchTransaction } from "~/composables/transactions/useTransaction";
import useUpdateTransaction from "~/composables/transactions/useUpdateTransaction";
import useCreateTransaction from "~/composables/transactions/useCreateTransaction";
import type { FormFilterTransaction } from "~/types/ui/component";
import useCompleteTransaction from "~/composables/transactions/useCompleteTransaction";
import { getLocalTimeZone } from "@internationalized/date";


const toast = useToast();

// const paramsTransactions = ref<FilterTransactionQuery>({
//     offset: 
// });

const page = ref(1);
const paramsTransactions = reactive<FilterTransactionQuery>({
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


const paramsBalance = reactive<FilterBalanceTransactionQuery>({
    accountFilterIds: [],
    categoryFilterIds: [],
    tagFilterIds: [],
    budgetFilterIds: [],
    maxPrice: undefined,
    minPrice: undefined,
    dateStart: undefined,
    dateEnd: undefined,
});

const {data: accounts } = useAccounts({
    limit: 0, offset: 0, queryAll: true
});
const {data: budgets, error: errorBudget, refresh: refreshBudget } = useBudgets({
    limit: 0, offset: 0, queryAll: true
});
const {data: categories, error: errorCategory, refresh: refreshCategory } = useCategories({
    limit: 0, offset: 0, queryAll: true
})
const {data: tags, error: errorTag, refresh: refreshTag } = useTags({
    limit: 0, offset: 0, queryAll: true
})



const {data:transactions, error: errorTransaction,  refresh:refreshTransactions } = useTransactionPagination(paramsTransactions);
const {data:balance, error: errorBalance, refresh:refreshBalance} = useBalance(paramsTransactions);

const displaytransactionsTable = computed(() => {
    const getCategory = (id: string) => categories.value?.items.find(i => id === i.id)
    const getTag = (id: string) => tags.value?.items.find(i => id === i.id)
    const getBudget = (id: string) => budgets.value?.items.find(i => id === i.id)

    return transactions.value?.items.map(i => ({
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
    } satisfies TransactionTableType))    
})

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
        refreshTransactions()
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
    refreshTransactions()
    refreshBalance() 
}

function onFilter(value: FormFilterTransaction) {
    paramsTransactions.categoryFilterIds = value.categoryIds
    paramsTransactions.tagFilterIds = value.tagIds
    paramsTransactions.accountFilterIds = value.accountIds
    paramsTransactions.budgetFilterIds = value.budgetIds
    paramsTransactions.dateEnd = value.dateEnd ? new Date(value.dateEnd) : undefined
    paramsTransactions.dateStart = value.dateStart ? new Date(value.dateStart) : undefined
    paramsTransactions.minPrice = value.minPrice
    paramsTransactions.maxPrice = value.minPrice
    paramsTransactions.status = value.status
    paramsTransactions.offset = 0
    page.value = 1
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
                    refreshTransactions()
                    refreshBalance()
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
                <UButton variant="outline" color="neutral" :label="formatCurrency(balance ? balance.balance : 0)" size="xl"/>
                <FilterTransactionDrawer @submit="onFilter" /> 

                <USwitch v-model="paramsTransactions.isFreeze"  label="Freeze Transactions"/>
            </div>

            <UButton icon="i-lucide-plus" label="Ajouter transaction" size="xl" @click="openTransaction()" />
        </div>

        <div class="mt-2">
            <div v-if="paramsTransactions.accountFilterIds?.length === 0">
                <UButton color="neutral" variant="outline" label="Tous les comptes"/>
            </div>
            <div class="flex flex-wrap gap-1" 
                v-if="paramsTransactions.accountFilterIds && paramsTransactions.accountFilterIds.length > 0">
                <div v-for="account of  accounts?.items.filter(i => paramsTransactions.accountFilterIds?.includes(i.id))" 
                    :key="account.id">
                    <UButton color="neutral" variant="outline" :label="account.title"/>
                </div>
            </div>
        </div> 

        <div style="margin-top: 1rem;" class="bg-white p-2 rounded-lg">
            <UTable 
                :data="displaytransactionsTable" 
                :columns="tableColumn" 
                class="flex-1">
                <template #expanded="{ row }">
                    <div class="flex flex-row flex-wrap">
                        <div v-for="tag in row.original.tags" :key="tag.id">
                            <UBadge :label="tag.value" :style="{color:tag.color, borderColor: tag.color}" variant="outline" color="neutral"/>  
                        </div>
                    </div>
                </template>
            </UTable>
            <div class="flex flex-row gap-2 items-baseline-last justify-between">
                <UPagination 
                    class="mt-3" 
                    v-model:page="page" 
                    v-on:update:page="v => paramsTransactions.offset = (paramsTransactions.limit * (v - 1))"
                    :items-per-page="paramsTransactions.limit"  
                    :total="transactions?.totals" 
                    active-variant="subtle" />
                <UInputNumber 
                    v-model="paramsTransactions.limit" 
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