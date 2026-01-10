<script setup lang="ts">
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'

import { reactive, shallowRef } from "vue";
import useAccounts from '~/composables/accounts/useAccounts';
import useCategories from '~/composables/categories/useCategories';
import useTags from '~/composables/tags/useTags';
import useTransactionTypes from '~/composables/internals/useTransactionTypes';
import type { FormError, FormSubmitEvent } from '#ui/types';
import type { EditScheduleTransactionType, ScheduleTransactionType } from '~/types/ui/scheduleTransaction';
import usePeriodTypes from '~/composables/internals/usePeriodTypes';

const { scheduleTransaction } = defineProps<{
    scheduleTransaction?: ScheduleTransactionType
}>();
const emit = defineEmits<{
    (e: 'submit', value: EditScheduleTransactionType, oldValue?: ScheduleTransactionType): void    
    (e: 'close', close: boolean): void
}>();

const {data: accounts} = useAccounts({
  queryAll: true,
  offset: 0,
  limit: 0
})
const {data: categories} = useCategories({
  queryAll: true,
  offset: 0,
  limit: 0
})
const {data: tags } = useTags({
  limit: 0,
  offset: 0,
  queryAll: true
})
const {data: transationTypes } = useTransactionTypes()
const {data: listPeriods, error, refresh} = usePeriodTypes();


const form = reactive<Partial<EditScheduleTransactionType>>({
    accountId: scheduleTransaction?.accountId || '',
    categoryId: scheduleTransaction?.categoryId || '',
    name: scheduleTransaction?.name || '',
    tagIds: scheduleTransaction?.tagIds || [],
    amount: scheduleTransaction?.amount || 0,
    repeater: scheduleTransaction?.repeater,
    type: scheduleTransaction?.type,
    isFreeze: false
})
const isRecurrence = ref(scheduleTransaction?.repeater !== undefined)
function onChangeIsRecurrence(isRecurrence: boolean) {
    form.repeater = isRecurrence ? (scheduleTransaction?.repeater || { period: "Day", interval: 1}) : undefined
}
let rawDueDate = scheduleTransaction ? scheduleTransaction.dueDate : new Date();
const dueDate = shallowRef(new CalendarDate(rawDueDate.getFullYear(), rawDueDate.getMonth() + 1, rawDueDate.getDate()));
const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
});

function validate(state: Partial<EditScheduleTransactionType>): FormError[] {
  const errors = []

  if (!state.name) errors.push({ name: 'name', message: 'Required' })
  if (!state.accountId) errors.push({ name: 'accountId', message: 'Required' })
  if (!state.categoryId) errors.push({ name: 'categoryId', message: 'Required' })
  if (!state.amount) errors.push({ name: 'amount', message: 'Required' })
  if (!dueDate.value) errors.push({ name: 'dueDate', message: 'Required' })
  if (!state.type) errors.push({ name: 'type', message: 'Required' })

  if (state.repeater && !state.repeater.period) errors.push({ name: 'period', message: 'Required' })
  if (state.repeater && !state.repeater.interval) errors.push({ name: 'interval', message: 'Required' })

  return errors
}

async function onSubmit(event: FormSubmitEvent<EditScheduleTransactionType>) {
    const data = event.data;
    emit('submit', {
        accountId: data.accountId!,
        amount: data.amount!,
        categoryId: !data.isFreeze ? data.categoryId! : undefined,
        name: data.name!,
        tagIds: data.tagIds!,
        type: !data.isFreeze ? data.type! : '',
        isFreeze: data.isFreeze,
        dueDate: dueDate.value, 
        repeater: form.repeater
    }, scheduleTransaction)

    form.accountId = "";
    form.amount = 0;
    form.categoryId = "";
    form.name = "";
    form.tagIds = [];
    form.type = "";
    form.repeater = undefined;
    form.dueDate = undefined;
    

    emit('close', true);
};

</script>

<template>
    <UModal title="Transaction">
        <template #body>
            <UForm :validate="validate" :state="form" class="space-y-4" @submit="onSubmit">
                <UFormField label="Est une freeze transaction" name="isFreeze">
                    <USwitch v-model="form.isFreeze" />
                </UFormField>

                <UFormField label="Type de transaction" name="type">
                    <USelect 
                        :disabled="form.isFreeze"
                        v-model="form.type"
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
                
                <UFormField label="Description" name="name">
                    <UInput v-model="form.name" class="w-full"/>
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

                <UFormField label="Date d'echeance" name="dueDate">
                    <UPopover>
                        <UButton color="neutral" variant="subtle" icon="i-lucide-calendar" >
                            {{ dueDate ? df.format(dueDate.toDate(getLocalTimeZone()))  : 'Selectionnez une de debut' }}
                        </UButton>
                        <template #content>
                            <UCalendar v-model="dueDate" />
                        </template>
                    </UPopover>
                </UFormField>
                
                <UFormField label="Est repete" name="isRecurrence">
                    <USwitch v-model="isRecurrence" @update:model-value="onChangeIsRecurrence" />
                </UFormField>

                <UFormField label="Periode" name="period" v-if="form.repeater">
                    <USelect 
                        v-model="form.repeater.period" 
                        value-key="value" 
                        :items="listPeriods?.map(i => ({ label: i.value, value: i.id}))"/>
                </UFormField>

                <UFormField label="Nombre de temps" name="periodTime" v-if="form.repeater">
                    <UInput v-model="form.repeater.interval" type="number" />
                </UFormField>
                
                <UFormField >
                    <UButton class="mr-2" type="submit" :label="scheduleTransaction ? 'Mettre a jour' : 'Ajouter'"  />
                    <UButton label="Annuler"  variant="outline" @click="emit('close', false)" />
                </UFormField>
            </UForm>
        </template>
    </UModal> 
</template>