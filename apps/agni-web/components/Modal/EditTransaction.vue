<script setup lang="ts">
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'

import * as z from 'zod';
import { reactive, shallowRef } from "vue";
import type { EditTransactionType, TransactionType } from '~/types/ui/transaction';
import useAccounts from '~/composables/accounts/useAccounts';
import useCategories from '~/composables/categories/useCategories';
import useTags from '~/composables/tags/useTags';
import useBudgets from '~/composables/budgets/useBudgets';
import useTransactionTypes from '~/composables/internals/useTransactionTypes';
import type { FormSubmitEvent } from '#ui/types';

const { transaction, accountSelectedId } = defineProps<{
    transaction?: TransactionType
    accountSelectedId?: string
}>();
const emit = defineEmits<{
    (e: 'submit', value: EditTransactionType, oldValue?: TransactionType): void    
    (e: 'close', close: boolean): void
}>();
const schema = z.object({
    accountId: z.string().nonempty('Vous devez selectionner un compte'),
    transactionType: z.string().nonempty('Vous devez selectionner une categories'),
    categoryId: z.string().nonempty('Vous devez selectionner une categories'),
    description: z.string().nonempty('Vous devez ajouter une description'),
    amount: z.number().min(1, 'La somme doit etre plus grand que zero'),
    tagIds: z.string().array(),
    budgetIds: z.string().array()
})

type Schema = z.output<typeof schema>;

const {data: accounts} = useAccounts()
const {data: categories} = useCategories()
const {data: tags } = useTags()
const {data: budgets} = useBudgets()
const {data: transationTypes } = useTransactionTypes()


const form = reactive<Partial<Schema>>({
    accountId: transaction?.accountId || (accountSelectedId || '') ,
    transactionType: transaction?.type || '',
    categoryId: transaction?.categoryId || '',
    description: transaction?.description || '',
    tagIds: transaction?.tagIds || [],
    budgetIds: transaction?.budgetIds || [],
    amount: transaction?.amount || 0
})


let valDate = transaction ? new Date(transaction.date)  : new Date();
const date = shallowRef(new CalendarDate(valDate.getFullYear(), valDate.getMonth() + 1, valDate.getDate()))

const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
});

async function onSubmit(event: FormSubmitEvent<Schema>) {
    const data = event.data;
    emit('submit', {
        accountId: data.accountId,
        type: data.transactionType,
        categoryId: data.categoryId,
        description: data.description,
        tagIds: data.tagIds,
        budgetIds: data.budgetIds,
        amount: data.amount,
        date: date.value
    }, transaction)

    form.accountId = "";
    form.amount = 0;
    form.budgetIds = [];
    form.categoryId = "";
    form.tagIds = [];
    form.transactionType = "";
    form.description = "";

    emit('close', true);
};

</script>

<template>
    <UModal title="Transaction">
        <template #body>
            <UForm :schema="schema" :state="form" class="space-y-4" @submit="onSubmit">
                <UFormField label="Type de transaction" name="transactionType">
                    <USelect 
                        v-model="form.transactionType"
                        value-key="value" 
                        :items="transationTypes?.map(i => ({ label: i.value, value: i.id}))" 
                        class="w-full"/>
                </UFormField>

                <UFormField label="Compte" name="accountId" >
                    <USelect 
                        v-model="form.accountId" 
                        value-key="value" 
                        :items="accounts?.items.map(i => ({value: i.id, label: i.title}))" 
                        class="w-full" />
                </UFormField>

                <UFormField label="Prix" name="amount">
                    <UInput v-model="form.amount" type="number" />
                </UFormField>
                
                <UFormField label="Description" name="description">
                    <UInput v-model="form.description" class="w-full"/>
                </UFormField>

                <UFormField label="Categorie" name="categoryId">
                    <USelectMenu 
                        v-model="form.categoryId" 
                        value-key="value" 
                        :items="categories?.items.map(i => ({value: i.id, label: i.title}))" 
                        class="w-full" />
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
                    <UInputMenu 
                        v-model="form.tagIds" 
                        multiple 
                        value-key="value"
                        :items="tags?.items.map(i => ({value: i.id, label: i.value}))" />
                </UFormField>

                <UFormField label="Budgets" name="budgetIds">
                    <UInputMenu 
                        v-model="form.budgetIds" 
                        multiple 
                        value-key="value" 
                        :items="budgets?.items.map(i => ({ value: i.id, label: i.title }))" />
                </UFormField>
                
                <UFormField >
                    <UButton class="mr-2" type="submit" :label="transaction ? 'Mettre a jour' : 'Ajouter'"  />
                    <UButton label="Annuler"  variant="outline" @click="emit('close', false)" />
                </UFormField>
            </UForm>
        </template>
    </UModal> 
</template>