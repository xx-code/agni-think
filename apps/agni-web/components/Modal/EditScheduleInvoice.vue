<script setup lang="ts">
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'

import { reactive, shallowRef } from "vue";
import type { FormError, FormSubmitEvent } from '#ui/types';
import type { EditScheduleInvoiceType, ScheduleInvoiceType } from '~/types/ui/scheduleTransaction';
import { fetchAccounts } from '~/composables/accounts/useAccounts';
import { fetchCategories } from '~/composables/categories/useCategories';
import { fetchTags } from '~/composables/tags/useTags';
import { fetchTransactionTypes } from '~/composables/internals/useTransactionTypes';
import { fetchPeriodTypes } from '~/composables/internals/usePeriodTypes';

const { scheduleInvoice } = defineProps<{
    scheduleInvoice?: ScheduleInvoiceType
}>();
const emit = defineEmits<{
    (e: 'submit', value: EditScheduleInvoiceType, oldValue?: ScheduleInvoiceType): void    
    (e: 'close', close: boolean): void
}>();

const { data: utils } = useAsyncData('utils+edit-invoices', async () => {
    const query = {offset: 0, limit: 0, queryAll: true, isSystem: false}
    const [ categories, tags, accounts, transactionTypes, periodTypes ] = await Promise.all([
        fetchCategories(query),
        fetchTags(query),
        fetchAccounts(query),
        fetchTransactionTypes(),
        fetchPeriodTypes()
    ])

    return {
        categories,
        tags,
        accounts,
        transactionTypes,
        periodTypes
    }
})

const form = reactive<Partial<EditScheduleInvoiceType>>({
    accountId: scheduleInvoice?.accountId || '',
    categoryId: scheduleInvoice?.categoryId || '',
    name: scheduleInvoice?.name || '',
    tagIds: scheduleInvoice?.tagIds || [],
    amount: scheduleInvoice?.amount || 0,
    repeater: scheduleInvoice?.repeater,
    type: scheduleInvoice?.type,
    isFreeze: false
})
const isRecurrence = ref(scheduleInvoice?.repeater !== undefined)
function onChangeIsRecurrence(isRecurrence: boolean) {
    form.repeater = isRecurrence ? (scheduleInvoice?.repeater || { period: "Day", interval: 1}) : undefined
}
let rawDueDate = scheduleInvoice ? scheduleInvoice.dueDate : new Date();
const dueDate = shallowRef(new CalendarDate(rawDueDate.getFullYear(), rawDueDate.getMonth() + 1, rawDueDate.getDate()));
const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
});

function validate(state: Partial<EditScheduleInvoiceType>): FormError[] {
  const errors = []

  if (!state.name) errors.push({ name: 'name', message: 'Required' })
  if (!state.accountId) errors.push({ name: 'accountId', message: 'Required' })
  if (!state.isFreeze && !state.categoryId) errors.push({ name: 'categoryId', message: 'Required' })
  if (!state.amount) errors.push({ name: 'amount', message: 'Required' })
  if (!dueDate.value) errors.push({ name: 'dueDate', message: 'Required' })
  if (!state.isFreeze && !state.type) errors.push({ name: 'type', message: 'Required' })

  if (state.repeater && !state.repeater.period) errors.push({ name: 'period', message: 'Required' })
  if (state.repeater && !state.repeater.interval) errors.push({ name: 'interval', message: 'Required' })

  return errors
}

async function onSubmit(event: FormSubmitEvent<EditScheduleInvoiceType>) {
    const data = event.data;
    emit('submit', {
        accountId: data.accountId!,
        amount: data.amount!,
        categoryId: !data.isFreeze ? data.categoryId! : undefined,
        name: data.name!,
        tagIds: data.tagIds!,
        type: !data.isFreeze ? data.type! : undefined,
        isFreeze: data.isFreeze,
        dueDate: dueDate.value, 
        repeater: form.repeater
    }, scheduleInvoice)

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
                        :items="utils?.transactionTypes.map(i => ({ label: i.value, value: i.id}))" 
                        class="w-full"/>
                </UFormField>

                <UFormField label="Compte" name="accountId" >
                    <USelect 
                        v-model="form.accountId" 
                        value-key="value" 
                        :items="utils?.accounts.items.map(i => ({value: i.id, label: i.title}))" 
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
                        :items="utils?.categories.items.map(i => ({value: i.id, label: i.title}))" 
                        class="w-full" />
                </UFormField> 

                <UFormField label="Tags" name="tagIds">
                    <UInputMenu 
                        :disabled="form.isFreeze"
                        v-model="form.tagIds" 
                        multiple 
                        value-key="value"
                        :items="utils?.tags.items.map(i => ({value: i.id, label: i.value}))" />
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
                        :items="utils?.periodTypes.map(i => ({ label: i.value, value: i.id}))"/>
                </UFormField>

                <UFormField label="Nombre de temps" name="periodTime" v-if="form.repeater">
                    <UInput v-model="form.repeater.interval" type="number" />
                </UFormField>
                
                <UFormField >
                    <UButton class="mr-2" type="submit" :label="scheduleInvoice ? 'Mettre a jour' : 'Ajouter'"  />
                    <UButton label="Annuler"  variant="outline" @click="emit('close', false)" />
                </UFormField>
            </UForm>
        </template>
    </UModal> 
</template>