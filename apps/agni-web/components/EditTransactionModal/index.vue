<script setup lang="ts">
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'

import * as z from 'zod';
import { reactive, ref, shallowRef } from "vue";
import { useFetchResumeAccount } from "../../composables/account";
import { useListBudget, type BudgetType } from '../../composables/budgets';
import { useFetchCategories } from '../../composables/categories';
import { useFetchTags, type TagType } from '../../composables/tags';
import type { FormSubmitEvent } from '@nuxt/ui';
import { useFetchMainCategories } from '../../composables/transactions';
const props = defineProps({
    isEdit: Boolean,
    accountId: String
})

const schema = z.object({
    accountId: z.string().nonempty('Vous devez selectionner un compte'),
    mainCategoryId: z.string().nonempty('Vous devez selectionner une categories'),
    categoryId: z.string().nonempty('Vous devez selectionner une categories'),
    description: z.string().nonempty('Vous devez ajouter une description'),
    amount: z.number().min(1, 'La somme doit etre plus grand que zero'),
})

type Schema = z.output<typeof schema>

const accounts = useFetchResumeAccount()

const categories = useFetchCategories()
const tags = useFetchTags()
const budgets = useListBudget()
const mainCategories = useFetchMainCategories()

const form = reactive({
    isCredit: false,
    accountId: props.accountId,
    mainCategoryId: '',
    categoryId: '',
    description: '',
    tagIds: [],
    budgetIds: [],
    amount: 0,
})

const date = shallowRef(new CalendarDate(new Date().getFullYear(), new Date().getMonth() + 1, new Date().getDate()))
const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
})

function onSubmit(event: FormSubmitEvent<Schema>) {
}

function onClose(){
    defineEmits({close})
}

</script>

<template>
<UModal 
title="Transaction"
:close="{ onClick: onClose}">
    <template #body>
        <UForm :schema="schema" :state="form" class="space-y-4" @submit="onSubmit">
            <UFormField label="" name="transactionType">
                <UTabs :content="form.isCredit" :items="[{label:'debit'}, {label:'credit'}]" class="w-full"/> 
            </UFormField>

            <UFormField label="Compte" name="accountId" >
                <USelect v-model="form.accountId" value-key="id" label-key="title" :items="accounts.filter(acc => acc.id !== ALL_ACCOUNT_ID)" class="w-full" />
            </UFormField>

            <UFormField label="Categorie de transaction" name="mainCategoryId">
                <USelect v-model="form.mainCategoryId" value-key="id" :items="mainCategories" class="w-full"/>
            </UFormField>

            <UFormField label="Prix" name="amount">
                <UInput v-model="form.amount" type="number" />
            </UFormField>
            
            <UFormField label="Description" name="description">
                <UInput v-model="form.description" class="w-full"/>
            </UFormField>

            <UFormField label="Categorie" name="categoryId">
                <USelectMenu v-model="form.categoryId" value-key="id" label-key="title" :items="categories" class="w-full"/>
            </UFormField>
            
            <UFormField label="Date" name="date">
                <UPopover>
                    <UButton color="neutral" variant="subtle" icon="i-lucide-calendar" >
                        {{ date ? df.format(date.toDate(getLocalTimeZone())) : 'Selectionnez une date' }}
                    </UButton>
                    <template #content>
                        <UCalendar v-model="date" />
                    </template>
                </UPopover>

                
            </UFormField>

            <UFormField label="Tags" name="tagIds">
                <UInputMenu v-model="form.tagIds" multiple value-key="id" label-key="value" :items="tags" />
            </UFormField>

            <UFormField label="Budgets" name="budgetIds">
                <UInputMenu v-model="form.budgetIds" multiple value-key="id" label-key="title" :items="budgets" />
            </UFormField>
            
            <UFormField >
                <UButton class="mr-2" type="submit" :label="isEdit ? 'Mettre a jour' : 'Ajouter'"  />
                <UButton label="Annuler"  variant="outline" />
            </UFormField>
        </UForm>
    </template>
    
</UModal> 

</template>