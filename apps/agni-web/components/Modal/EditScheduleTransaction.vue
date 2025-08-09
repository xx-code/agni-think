<script setup lang="ts">
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'

import * as z from 'zod';
import { reactive, shallowRef } from "vue";
import useAccounts from '~/composables/accounts/useAccounts';
import useCategories from '~/composables/categories/useCategories';
import useTags from '~/composables/tags/useTags';
import useTransactionTypes from '~/composables/internals/useTransactionTypes';
import type { FormSubmitEvent } from '#ui/types';
import type { EditScheduleTransactionType, ScheduleTransactionType } from '~/types/ui/scheduleTransaction';
import usePeriodTypes from '~/composables/internals/usePeriodTypes';

const { scheduleTransaction } = defineProps<{
    scheduleTransaction?: ScheduleTransactionType
}>();
const emit = defineEmits<{
    (e: 'submit', value: EditScheduleTransactionType, oldValue?: ScheduleTransactionType): void    
    (e: 'close', close: boolean): void
}>();
const schema = z.object({
    accountId: z.string().nonempty('Vous devez selectionner un compte'),
    transactionType: z.string().nonempty('Vous devez selectionner une categories'),
    categoryId: z.string().nonempty('Vous devez selectionner une categories'),
    description: z.string().nonempty('Vous devez ajouter une description'),
    amount: z.number().min(1, 'La somme doit etre plus grand que zero'),
    period: z.string().nonempty("Vous devez selectionner une period"),
    periodTime: z.number().min(1, "Vous devez selectionner un nombre de periode"),
    tagIds: z.string().array()
})

type Schema = z.output<typeof schema>;

const {data: accounts} = useAccounts()
const {data: categories} = useCategories()
const {data: tags } = useTags()
const {data: transationTypes } = useTransactionTypes()
const {data: listPeriods, error, refresh} = usePeriodTypes();


const form = reactive({
    accountId: scheduleTransaction?.accountId || '',
    transactionType: scheduleTransaction?.type || '',
    categoryId: scheduleTransaction?.categoryId || '',
    description: scheduleTransaction?.name || '',
    tagIds: scheduleTransaction?.tagIds || [],
    amount: scheduleTransaction?.amount || 0,
    period: scheduleTransaction?.period || '',
    periodTime: scheduleTransaction?.periodTime || 0,
    hasEndDate: false
})


let startDated = scheduleTransaction ? new Date(scheduleTransaction.dateStart)  : new Date();
let endDated: Date|undefined; 
if (scheduleTransaction?.dateEnd)
    endDated = new Date(scheduleTransaction.dateEnd);

const startDate = shallowRef(new CalendarDate(startDated.getFullYear(), startDated.getMonth() + 1, startDated.getDate()))
const endDate = shallowRef(endDated ?new CalendarDate(endDated.getFullYear(), endDated.getMonth() + 1, endDated.getDate()) : undefined);

const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
});

async function onSubmit(event: FormSubmitEvent<Schema>) {
    const data = event.data;
    emit('submit', {
        accountId: data.accountId,
        amount: data.amount,
        categoryId: data.categoryId,
        dateStart: startDate.value,
        dateEnd: endDate.value,
        name: data.description,
        tagIds: data.tagIds,
        type: data.transactionType,
        period: data.period,
        periodTime: data.periodTime,
    }, scheduleTransaction)

    form.accountId = "";
    form.amount = 0;
    form.categoryId = "";
    form.hasEndDate = false;
    form.description = "";
    form.tagIds = [];
    form.transactionType = "";
    form.period = "";
    form.periodTime = 0;
    
    endDate.value = undefined;

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

                <UFormField label="Tags" name="tagIds">
                    <UInputMenu 
                        v-model="form.tagIds" 
                        multiple 
                        value-key="value"
                        :items="tags?.items.map(i => ({value: i.id, label: i.value}))" />
                </UFormField> 

                <UFormField label="Date de debut" name="startDate">
                    <UPopover>
                        <UButton color="neutral" variant="subtle" icon="i-lucide-calendar" >
                            {{ startDate ? df.format(startDate.toDate(getLocalTimeZone())) : 'Selectionnez une de debut' }}
                        </UButton>
                        <template #content>
                            <UCalendar v-model="startDate" />
                        </template>
                    </UPopover>
                </UFormField>
                
                <UFormField label="Date de fin" name="endDate">
                    <USwitch v-model="form.hasEndDate" />
                </UFormField>

                <UFormField label="Date de fin" name="endDate" v-if="form.hasEndDate">
                    <UPopover>
                        <UButton color="neutral" variant="subtle" icon="i-lucide-calendar" >
                            {{ endDate ? df.format(endDate.toDate(getLocalTimeZone())) : 'Selectionnez une de fin' }}
                        </UButton>
                        <template #content>
                            <UCalendar v-model="endDate" />
                        </template>
                    </UPopover>
                </UFormField>

                
                <UFormField label="Periode" name="period">
                    <USelect 
                        v-model="form.period" 
                        value-key="value" 
                        :items="listPeriods?.map(i => ({ label: i.value, value: i.id}))"/>
                </UFormField>

                <UFormField label="Nombre de temps" name="periodTime">
                    <UInput v-model="form.periodTime" type="number" />
                </UFormField>
                
                <UFormField >
                    <UButton class="mr-2" type="submit" :label="scheduleTransaction ? 'Mettre a jour' : 'Ajouter'"  />
                    <UButton label="Annuler"  variant="outline" @click="emit('close', false)" />
                </UFormField>
            </UForm>
        </template>
    </UModal> 
</template>