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
    accountId: z.string().optional(),
    transactionType: z.string().optional(),
    categoryId: z.string().optional(),
    description: z.string().optional(),
    amount: z.number().optional(),
    period: z.string().optional(),
    periodTime: z.number().optional(),
    tagIds: z.array(z.string()).optional(),
    isFreeze: z.boolean(),
}).superRefine((obj, ctx) => {
    if (obj.isFreeze) {
    // When freeze is true:
    // accountId, period, periodTime, amount, description REQUIRED
    if (!obj.accountId || obj.accountId.trim() === "") {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Vous devez selectionner un compte (freeze activé)",
        path: ["accountId"],
      });
    }
    if (!obj.period || obj.period.trim() === "") {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Vous devez selectionner une période (freeze activé)",
        path: ["period"],
      });
    }
    if (obj.periodTime === undefined || obj.periodTime < 1) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Vous devez selectionner un nombre de période >= 1 (freeze activé)",
        path: ["periodTime"],
      });
    }
    if (obj.amount === undefined || obj.amount < 1) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "La somme doit être plus grande que zéro (freeze activé)",
        path: ["amount"],
      });
    }
    if (!obj.description || obj.description.trim() === "") {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Vous devez ajouter une description (freeze activé)",
        path: ["description"],
      });
    }
    // transactionType, categoryId, tagIds are OPTIONAL in freeze mode, no check
  } else {
    // When freeze is false:
    if (!obj.accountId || obj.accountId.trim() === "") {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Vous devez selectionner un compte (freeze activé)",
        path: ["accountId"],
      });
    }
    if (!obj.period || obj.period.trim() === "") {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Vous devez selectionner une période (freeze activé)",
        path: ["period"],
      });
    }
    if (obj.periodTime === undefined || obj.periodTime < 1) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Vous devez selectionner un nombre de période >= 1 (freeze activé)",
        path: ["periodTime"],
      });
    }
    if (obj.amount === undefined || obj.amount < 1) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "La somme doit être plus grande que zéro (freeze activé)",
        path: ["amount"],
      });
    }
    if (!obj.description || obj.description.trim() === "") {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Vous devez ajouter une description (freeze activé)",
        path: ["description"],
      });
    }
    if (!obj.transactionType || obj.transactionType.trim() === "") {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Vous devez selectionner une catégorie",
        path: ["transactionType"],
      });
    }
    if (!obj.categoryId || obj.categoryId.trim() === "") {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "Vous devez selectionner une catégorie",
        path: ["categoryId"],
      });
    }
    // accountId, period, periodTime, amount, description optional or not required in freeze false mode
  }
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
    isFreeze: false,
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
        accountId: data.accountId!,
        amount: data.amount!,
        categoryId: !data.isFreeze ? data.categoryId! : 'X',
        dateStart: startDate.value,
        dateEnd: endDate.value,
        name: data.description!,
        tagIds: data.tagIds!,
        type: !data.isFreeze ? data.transactionType!  : 'X',
        period: data.period!,
        isFreeze: data.isFreeze,
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
                <UFormField label="Est une freeze transaction" name="isFreeze">
                    <USwitch v-model="form.isFreeze" />
                </UFormField>

                <UFormField label="Type de transaction" name="transactionType">
                    <USelect 
                        :disabled="form.isFreeze"
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
                        :disabled="form.isFreeze"
                        v-model="form.categoryId" 
                        value-key="value" 
                        :items="categories?.items.map(i => ({value: i.id, label: i.title}))" 
                        class="w-full" />
                </UFormField> 

                <UFormField label="Tags" name="tagIds">
                    <UInputMenu 
                        :disabled="form.isFreeze"
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