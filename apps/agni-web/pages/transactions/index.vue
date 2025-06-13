<script setup lang="ts">
import { computed, onMounted, ref, shallowRef, watch, watchEffect, type Ref } from "vue"; import { ALL_ACCOUNT_ID, useFetchResumeAccount } from "../../composables/account"; import type { DropdownMenuItem } from "@nuxt/ui"; import { useFetchListCategories, type CategoryType } from "../../composables/categories";
import { useFetchListTags, type TagType } from "../../composables/tags";
import { useFetchListBudget, type BudgetType } from "../../composables/budgets";
import { fetchBalance, fetchDeleteTransaction, fetchListTransaction, useFetchBalance, useFetchListTransactions } from "../../composables/transactions";
import { EditTransactionModal } from "#components";

const selectedBudgetIds = ref<string[]>([])
const selectedCategoryIds = ref<string[]>([])
const selectedTagIds = ref<string[]>([])
const filterSelected = ref({
    'category': false, 'tag': false, 'date': false,
    'price': false, 'budget': false
})
const page = ref(1)
const maxPage = ref(1)
const nbItems = ref(8)
const selectedAccounts: Ref<{id: string, label: string, checked: boolean}[]> = ref([]) 

const paramsTransactions = computed(() => ({
  page: page.value,
  limit: nbItems.value,
  accountFilter: selectedAccounts.value.filter(acc => acc.checked).map(acc => acc.id),
  categoryFilter: selectedCategoryIds.value,
  tagFilter: selectedTagIds.value,
  budgetFilter: selectedBudgetIds.value
}))

const paramsBalance = computed(() => ({
  accountIds: selectedAccounts.value.filter(acc => acc.checked).map(acc => acc.id),
  categoryIds: selectedCategoryIds.value,
  tagIds: selectedTagIds.value,
  budgetIds: selectedBudgetIds.value
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
}

const accountsDropdown = computed(() =>{
   let base = [
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

    let otherAccount = [] 
    if (accounts.value) 
        otherAccount = accounts.value.filter(acc => acc.id !== ALL_ACCOUNT_ID)
        .map(acc => ({
            label: acc.title, 
            checked: selectedAccounts.value.find(selAcc => selAcc.id == acc.id)?.checked, 
            type: 'checkbox' as const, 
            onselect(e:Event) {
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
        }
    },
    {
        label: 'Tags',
        type: 'checkbox' as const,
        checked: filterSelected.value['tag'],
        onUpdateChecked(checked: boolean) {
            filterSelected.value['tag'] = checked
        }
    },
    {
        label: 'Date',
        type: 'checkbox' as const,
        checked: filterSelected.value['date'],
        onUpdateChecked(checked: boolean) {
            filterSelected.value['date'] = checked
        }
    }, 
    {
        label: 'Prix',
        type: 'checkbox' as const,
        checked: filterSelected.value['price'],
        onUpdateChecked(checked: boolean) {
            filterSelected.value['price'] = checked
        }
    },
    {
        label: 'Budgets',
        type: 'checkbox' as const,
        checked: filterSelected.value['budget'],
        onUpdateChecked(checked: boolean) {
            filterSelected.value['budget'] = checked
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
watch([paramsTransactions, paramsBalance, selectedAccounts],
    async () => { 
        if (transactions.value)
            transactions.value = await fetchListTransaction(paramsTransactions.value)
        
        if (balance.value)
            balance.value = await fetchBalance(paramsBalance.value)

        if (transactions.value)
            maxPage.value = transactions.value.maxPage

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

</script>

<template>
    <div>
        <div class="flex justify-between flex-wrap" style="margin-top: 1rem;">
            <div class="flex items-center gap-3">
                <UDropdownMenu :items="accountsDropdown">
                    <UButton color="neutral" variant="outline" icon="i-lucide-menu" label="Comptes" size="xl"/>
                </UDropdownMenu>
                <UButton variant="outline" color="neutral" :label="'$' + balance" size="xl"/>
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
                    <UInput placeholder="Min" type="number" :min="0"/>
                    <UInput placeholder="Max" type="number" :min="0" />
                </div>
            </div>
        </div>

        <div style="margin-top: 1rem;">
            <div class="transaction-box flex flex-col gap-2 rounded-md">
                <div v-for="trans of listTransaction" :key="trans.id">
                    <RowTransaction 
                        :id="trans.id" 
                        :balance="trans.amount"
                        :title="trans.category.title"
                        :description="trans.description"
                        :icon="trans.category.icon"
                        :record-type="trans.recordType"
                        :doShowEdit="true"
                        :date="trans.date"
                        :tags="trans.tags.map(tag => tag.value)"
                        @update="(id) => onEditTransaction(id)"
                        @delete="(id) => onDelete(id)"
                     />
                </div>
            </div>
            <UPagination class="mt-3" v-model:page="page" :total="maxPage" />
        </div>
    </div>
</template>