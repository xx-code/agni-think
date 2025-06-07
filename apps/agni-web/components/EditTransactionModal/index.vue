<script setup lang="ts">
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'

import * as z from 'zod';
import { reactive, ref, shallowRef } from "vue";
import { fetchListAccounts, useFetchResumeAccount } from "../../composables/account";
import { useFetchListBudget, type BudgetType } from '../../composables/budgets';
import { useFetchListCategories } from '../../composables/categories';
import { useFetchListTags, type TagType } from '../../composables/tags';
import type { FormSubmitEvent } from '@nuxt/ui';
import { fetchCreateTransaction, fetchUpdateTransaction, useFetchListTransactionType, useFetchTransaction } from '../../composables/transactions';
const props = defineProps({
    isEdit: Boolean,
    transactionId: String,
    onSaved: Function
})

const schema = z.object({
    accountId: z.string().nonempty('Vous devez selectionner un compte'),
    transactionType: z.string().nonempty('Vous devez selectionner une categories'),
    categoryId: z.string().nonempty('Vous devez selectionner une categories'),
    description: z.string().nonempty('Vous devez ajouter une description'),
    amount: z.number().min(1, 'La somme doit etre plus grand que zero'),
})

type Schema = z.output<typeof schema>

const accounts = await fetchListAccounts()

const categories = await useFetchListCategories()
const tags = await useFetchListTags()
const budgets = await useFetchListBudget()
const mainCategories = await useFetchListTransactionType()

let transaction = null
if (props.transactionId)
    transaction = await useFetchTransaction(props.transactionId)

const form = reactive({
    accountId: transaction ? transaction.value?.accountId ?? '' : '',
    transactionType: transaction ? transaction.value?.type ?? '' : '',
    categoryId: transaction ? transaction.value?.category.id ?? '' : '',
    description: transaction ? transaction.value?.description ?? '': '',
    tagIds: transaction ? transaction.value?.tags.map(tag => tag.id) ?? [] : [],
    budgetIds: transaction ? transaction.value?.budgets.map(budg => budg.id) ?? [] : [],
    amount: transaction ? transaction.value?.amount ?? 0 : 0
})

let valDate = new Date()
if (transaction)
    valDate = new Date(transaction.value?.date ?? '')

const date = shallowRef(new CalendarDate(valDate.getFullYear(), valDate.getMonth() + 1, valDate.getDate()))
const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
})

const emit = defineEmits(['close'])

async function onSubmit(event: FormSubmitEvent<Schema>) {
    if (!props.isEdit) {
        await fetchCreateTransaction({
            description: form.description, 
            categoryId: form.categoryId, 
            accountId: form.accountId,
            type: form.transactionType,
            amount: form.amount,
            date: date.value.toString(),
            budgetIds: form.budgetIds,
            tagIds: form.tagIds
        })
    } else {
        await fetchUpdateTransaction({
            transactionId: props.transactionId ?? '',
            description: form.description,
            categoryId: form.categoryId,
            accountId: form.accountId,
            type: form.transactionType,
            amount: form.amount,
            date: date.value.toString(),
            budgetIds: form.budgetIds,
            tagIds: form.tagIds
        })
    } 
    
    if (props.onSaved) props.onSaved()

    emit('close')
}


</script>

<template>
<UModal title="Transaction">
    <template #body>
        <UForm :schema="schema" :state="form" class="space-y-4" @submit="onSubmit">
            <UFormField label="Type de transaction" name="transactionType">
                <USelect v-model="form.transactionType" value-key="id" :items="mainCategories" class="w-full"/>
            </UFormField>

            <UFormField label="Compte" name="accountId" >
                <USelect v-model="form.accountId" value-key="id" label-key="title" :items="accounts.filter(acc => acc.id !== ALL_ACCOUNT_ID)" class="w-full" />
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
                <UButton label="Annuler"  variant="outline" @click="emit('close')" />
            </UFormField>
        </UForm>
    </template>
    
</UModal> 

</template>