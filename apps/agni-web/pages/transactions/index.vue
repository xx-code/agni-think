<script setup lang="ts">
import { computed, h, onMounted, ref, resolveComponent, shallowRef, watch, watchEffect, type Ref } from "vue"; 
import { ALL_ACCOUNT_ID, useFetchResumeAccount } from "../../composables/account"; 
import type { DropdownMenuItem, TableColumn, TableRow } from "@nuxt/ui"; 
import { useFetchListTags, type TagType } from "../../composables/tags";
import { useFetchListBudget, type BudgetType } from "../../composables/budgets";
import { fetchBalance, fetchDeleteTransaction, fetchListTransaction, type TransactionType, useFetchBalance, useFetchListTransactions } from "../../composables/transactions";
import { EditTransactionModal } from "#components";
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'
import { useFetchListCategories } from "../../composables/categories";
import { formatCurrency } from "../../composables/utils";

const selectedBudgetIds = ref<string[]>([])
const selectedCategoryIds = ref<string[]>([])
const selectedTagIds = ref<string[]>([])
const filterSelected = ref({
    'category': false, 'tag': false, 'date': false,
    'price': false, 'budget': false
})

const df = new DateFormatter('en-US', {
  dateStyle: 'medium'
})

const date = shallowRef({
  start: new CalendarDate(new Date().getFullYear(), new Date().getMonth() + 1, new Date().getDate()),
  end: new CalendarDate(new Date().getFullYear(), new Date().getMonth() + 1, new Date().getDate())
})

const useDate = ref(false)

const minAmount = ref(0)
const maxAmount = ref(0)

const page = ref(1)
const nbItems = ref(8)
const selectedAccounts: Ref<{id: string, label: string, checked: boolean}[]> = ref([]) 

const paramsTransactions = computed(() => ({
  page: page.value,
  limit: nbItems.value,
  accountFilter: selectedAccounts.value.filter(acc => acc.checked).map(acc => acc.id),
  categoryFilter: selectedCategoryIds.value,
  tagFilter: selectedTagIds.value,
  budgetFilter: selectedBudgetIds.value,
  minAmount: minAmount.value,
  maxAmount: maxAmount.value,
  dateStart: useDate.value ? date.value.start.toString() : '',
  dateEnd: useDate.value ? date.value.end.toString() : '',
}))

const paramsBalance = computed(() => ({
  accountIds: selectedAccounts.value.filter(acc => acc.checked).map(acc => acc.id),
  categoryIds: selectedCategoryIds.value,
  tagIds: selectedTagIds.value,
  budgetIds: selectedBudgetIds.value,
  maxAmount: filterSelected.value.price ? maxAmount.value : undefined,
  minAmount: filterSelected.value.price ? minAmount.value : undefined,
  dateStart: useDate.value ? date.value.start.toString() : '',
  dateEnd: useDate.value ? date.value.end.toString() : '',
}))

const {data: accounts} = useFetchResumeAccount()
const {data: budgets} = useFetchListBudget()
const {data: categories} = useFetchListCategories()
const {data: tags }= useFetchListTags()



const {data:transactions, refresh:refreshTransactions} = useFetchListTransactions(paramsTransactions.value)

const {data:balance, refresh:refreshBalance} = useFetchBalance(paramsBalance.value)

const overlay = useOverlay()
const modalTransaction = overlay.create(EditTransactionModal, {
    props: {
        onSaved: async () => {
            transactions.value = await fetchListTransaction(paramsTransactions.value)
            balance.value = await fetchBalance(paramsBalance.value)
        }
    }
})



const onUpdateChecked = (id: string, checked: boolean) => {
    const idx = selectedAccounts.value.findIndex(acc => acc.id === id)
    if (idx !== -1)
        selectedAccounts.value[idx].checked = checked
    if (selectedAccounts.value.every(val => val.checked === false))
        for(let i = 0; i < selectedAccounts.value.length; i++)
            selectedAccounts.value[i].checked = true
}

const accountsDropdown = computed(() =>{
   let base: DropdownMenuItem[] = [
        {
            label: 'Tous les comptes', 
            type: 'checkbox' as const,
            onSelect(e: Event) {
                e.preventDefault()
                selectedAccounts.value.forEach(acc => acc.checked = true)
            }
        },
        {
            type: 'separator' as const,
        },
    ]

    let otherAccount: DropdownMenuItem[] = [] 
    if (accounts.value) 
        otherAccount = accounts.value.filter(acc => acc.id !== ALL_ACCOUNT_ID)
        .map(acc => ({
            label: acc.title, 
            checked: selectedAccounts.value.find(selAcc => selAcc.id == acc.id)?.checked, 
            type: 'checkbox' as const, 
            onSelect(e:Event) {
                e.preventDefault()
            },
            onUpdateChecked(checked: boolean) {
                onUpdateChecked(acc.id, checked)
            }
        }))

    return (base.concat(otherAccount) satisfies DropdownMenuItem[]) 
}) 




const filtersDropdown = computed(() => [
    {
        label: 'Categories',
        type: 'checkbox' as const,
        checked: filterSelected.value['category'],
        onUpdateChecked(checked: boolean) {
            filterSelected.value['category'] = checked
            selectedCategoryIds.value = []
        }
    },
    {
        label: 'Tags',
        type: 'checkbox' as const,
        checked: filterSelected.value['tag'],
        onUpdateChecked(checked: boolean) {
            filterSelected.value['tag'] = checked
            selectedTagIds.value = []
        }
    },
    {
        label: 'Date',
        type: 'checkbox' as const,
        checked: filterSelected.value['date'],
        onUpdateChecked(checked: boolean) {
            useDate.value = checked
            filterSelected.value['date'] = checked
        }
    }, 
    {
        label: 'Prix',
        type: 'checkbox' as const,
        checked: filterSelected.value['price'],
        onUpdateChecked(checked: boolean) {
            filterSelected.value['price'] = checked
            maxAmount.value = 0
            minAmount.value = 0
        }
    },
    {
        label: 'Budgets',
        type: 'checkbox' as const,
        checked: filterSelected.value['budget'],
        onUpdateChecked(checked: boolean) {
            filterSelected.value['budget'] = checked
            selectedBudgetIds.value = []
        }
    },
    {
        type: 'separator'
    },
    {
        label: 'supprimer tous',
        type: 'checkbox' as const,
        onUpdateChecked(checked: boolean) {
            filterSelected.value ={
                'category': false, 'tag': false, 'date': false,
                'price': false, 'budget': false
            }
            selectedAccounts.value = accounts.value.filter(acc => acc.id !== ALL_ACCOUNT_ID).map(acc => ({id: acc.id, label: acc.title, checked: true}))
            selectedCategoryIds.value = []
            selectedTagIds.value = []
            selectedBudgetIds.value = []
        }
    }
]satisfies DropdownMenuItem[])

const onEditTransaction = (id: string|null=null) => {
    if(id){
        modalTransaction.patch({isEdit: true, transactionId: id})
    } else {
        modalTransaction.patch({isEdit: false, transactionId: ''})
    }
   
    modalTransaction.open()
}

const onDelete = async (id: string) => {
    await fetchDeleteTransaction(id)
    transactions.value = await fetchListTransaction(paramsTransactions.value)
    balance.value = await fetchBalance(paramsBalance.value)
}

// onMounted(() => {
//     if(accounts.value || selectedAccounts.value.every(acc => acc.checked == false))
//             selectedAccounts.value = accounts.value.filter(acc => acc.id !== ALL_ACCOUNT_ID)
//                     .map(acc => ({id: acc.id, label: acc.title, checked: true}))
// })
let hasInitializedAccounts = false
watch([paramsTransactions, paramsBalance, selectedAccounts, maxAmount, minAmount, date, nbItems, page],
    async () => { 
        if (transactions.value) {
            const fetchedData = await fetchListTransaction(paramsTransactions.value)
            transactions.value = fetchedData;
        }
        
        if (balance.value)
            balance.value = await fetchBalance(paramsBalance.value)

       if (!hasInitializedAccounts && accounts.value) {
            hasInitializedAccounts = true
            selectedAccounts.value = accounts.value
                .filter(acc => acc.id !== ALL_ACCOUNT_ID)
                .map(acc => ({ id: acc.id, label: acc.title, checked: true }))
        } 

        
}, { immediate: true, deep: true})

const listTransaction = computed(() => {
    if (transactions.value)
        return transactions.value.transactions
    return []
})

const UIcon = resolveComponent('UIcon')
const UButton = resolveComponent('UButton')
const UDropdownMenu = resolveComponent('UDropdownMenu')
const tableColumn: TableColumn<TransactionType>[] = [
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
        accessorKey: 'category.icon',
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
            return new Date(row.getValue('date')).toLocaleString('fr-FR', {
                day: 'numeric',
                month: 'short',
                hour: '2-digit',
                minute: '2-digit',
                hour12: false
            })
        }
    },
    {
        accessorKey: 'category.title',
        header: 'Categorie'
    },
    {
        accessorKey: 'description',
        header: 'Desription',
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
const expanded = ref({ 1: true })

function getRowItems(rows: TableRow<TransactionType>) {
    return [
        {
            label: 'Modifier',
            onSelect: () => {
                const id = rows.original.id
                onEditTransaction(id)
            }
        },
        {
            label: 'Supprimer',
            onselect: () => {
                const id = rows.original.id
                if (confirm("Voulez vous supprimer la transaction"))
                    onDelete(id)
            }
        }
    ] 
}


</script>

<template>
    <div>
        <div class="flex justify-between flex-wrap" style="margin-top: 1rem;">
            <div class="flex items-center gap-3">
                <UDropdownMenu :items="accountsDropdown">
                    <UButton color="neutral" variant="outline" icon="i-lucide-menu" label="Comptes" size="xl"/>
                </UDropdownMenu>
                <UButton variant="outline" color="neutral" :label="formatCurrency(balance ? balance : 0)" size="xl"/>
                <UDropdownMenu class="xs:mt-2" :items="filtersDropdown">
                    <UButton color="neutral" variant="outline" icon="i-lucide-sliders-horizontal" size="xl" label="Filtres"/>
                </UDropdownMenu>
            </div>

            <UButton icon="i-lucide-plus" label="Ajouter transaction" size="xl" @click="onEditTransaction()" />
        </div>

        <div class="mt-2">
            <div v-if="selectedAccounts.filter(acc => acc.checked).length === selectedAccounts.length">
                <UButton color="neutral" variant="outline" label="Tous les comptes"/>
            </div>
            <div class="flex flex-wrap gap-1" v-if="selectedAccounts.filter(acc => acc.checked).length !== selectedAccounts.length">
                <div v-for="account of selectedAccounts.filter(acc => acc.checked)" :key="account.id">
                    <UButton color="neutral" variant="outline" :label="account.label"/>
                </div>
            </div>
        </div>

        <div class="flex flex-wrap gap-2 mt-2 items-center">
            <div v-if="filterSelected.category">
                <UInputMenu placeholder="Categories" multiple v-model="selectedCategoryIds" mutliple value-key="id" label-key="title" :items="categories"/>
            </div>

            <div v-if="filterSelected.tag">
                <UInputMenu placeholder="Tags" multiple v-model="selectedTagIds" mutliple  value-key="id" label-key="value" :items="tags"/>
            </div> 
            
            <div v-if="filterSelected.budget">
                <UInputMenu placeholder="Budgets" multiple v-model="selectedBudgetIds" mutliple value-key="id" label-key="title" :items="budgets"/>
            </div> 
            
            <div>
                <MultiCalendarSelection v-if="filterSelected.date" />
            </div>

            <div v-if="filterSelected.price" >
                <div class="flex gap-1">
                    <UInput placeholder="Min" v-model="minAmount" type="number" :min="0"/>
                    <UInput placeholder="Max" v-model="maxAmount" type="number" :min="minAmount" />
                </div>
            </div>
        </div>

        <div style="margin-top: 1rem;">
            <UTable 
                :data="transactions ? transactions.transactions : []" 
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
                <UPagination class="mt-3" v-model:page="page" :items-per-page="nbItems"  :total="transactions ? transactions.total : 1" active-variant="subtle"/>
                <UInputNumber v-model="nbItems" :min="1" orientation="vertical" style="width: 80px;"/>
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