<script setup lang="ts">
import { computed, ref } from "vue";
import { ALL_ACCOUNT_ID, useFetchResumeAccount } from "../../composables/account";
import type { DropdownMenuItem } from "@nuxt/ui";
import { useFetchCategories } from "../../composables/categories";
import { useFetchTags } from "../../composables/tags";
import { useListBudget } from "../../composables/budgets";

const accounts = useFetchResumeAccount()
const selectedAccounts = ref(accounts.value.filter(acc => acc.id !== ALL_ACCOUNT_ID)
.map(acc => ({id: acc.id, label: acc.title, checked: true})))

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

    const otherAccount =  accounts.value.filter(acc => acc.id !== ALL_ACCOUNT_ID)
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

const budgets = useListBudget()
const selectedBudgetIds = ref([])
const categories = useFetchCategories()
const selectedCategoryIds = ref([])
const tags = useFetchTags()
const selectedTagIds = ref([])

const filterSelected = ref({
    'category': false, 'tag': false, 'date': false,
    'price': false, 'budget': false
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
        }
    }
])


</script>

<template>
    <div>
        <div class="flex justify-between" style="margin-top: 1rem;">
            <div class="flex items-center gap-3">
                <UDropdownMenu :items="accountsDropdown">
                    <UButton color="neutral" variant="outline" icon="i-lucide-menu" label="Comptes" size="xl"/>
                </UDropdownMenu>
                <UButton variant="outline" color="neutral" label="value" size="xl"/>
                <UDropdownMenu :items="filtersDropdown">
                    <UButton color="neutral" variant="outline" icon="i-lucide-sliders-horizontal" size="xl" label="Filtres"/>
                </UDropdownMenu>
            </div>

            <UButton icon="i-lucide-plus" label="Ajouter transaction" size="xl" />
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
            <UFormField v-if="filterSelected.category">
                <UInputMenu placeholder="Categories" multiple :v-model="selectedCategoryIds" mutliple value-key="id" label-key="title" :items="categories"/>
            </UFormField>

            <UFormField v-if="filterSelected.tag">
                <UInputMenu placeholder="Tags" multiple :v-model="selectedTagIds" mutliple value-key="id" label-key="value" :items="tags"/>
            </UFormField> 
            
            <UFormField v-if="filterSelected.budget">
                <UInputMenu placeholder="Budgets" multiple :v-model="selectedBudgetIds" mutliple value-key="id" label-key="title" :items="budgets"/>
            </UFormField> 
            
            <UFormField>
                <MultiCalendarSelection v-if="filterSelected.date" />
            </UFormField>

            <UFormField v-if="filterSelected.price" >
                <div class="flex gap-1">
                    <UInput placeholder="Min" type="number" :min="0"/>
                    <UInput placeholder="Max" type="number" :min="0" />
                </div>
            </UFormField>
        </div>
    </div>
</template>