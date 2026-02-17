<script setup lang="ts">
import { reactive } from "vue";
import type { FormError, FormSubmitEvent } from '@nuxt/ui';
import type { EditProvisionType, ProvisionType } from '~/types/ui/provision';
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date';

const { provision } = defineProps<{
    provision?: ProvisionType
}>();

const emit = defineEmits<{
    (e: 'submit', value: EditProvisionType, oldValue?: ProvisionType): void    
    (e: 'close', close: boolean): void
}>();


function validate(state: Partial<EditProvisionType>): FormError[] {
  const errors = []

  if (!state.title) errors.push({ name: 'title', message: 'Required' })
  if (!acquisitionDate.value) errors.push({ name: 'acquisitionDate', message: 'Required' })
  if (!state.expectedLifespanMonth) errors.push({ name: 'expectedLifespanMonth', message: 'Required' })
  if (!state.residualValue) errors.push({ name: 'residualValue', message: 'Required' })

  return errors
}

const form = reactive<Partial<EditProvisionType>>({
    title: provision?.title,
    initialCost: provision?.initialCost,
    expectedLifespanMonth: provision?.expectedLifespanMonth,
    residualValue: provision?.residualValue
});

let rawDueDate = provision ? provision.acquisitionDate : new Date();
const acquisitionDate = shallowRef(new CalendarDate(rawDueDate.getFullYear(), rawDueDate.getMonth() + 1, rawDueDate.getDate()));
const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
});

async function onSubmit(event: FormSubmitEvent<EditProvisionType>) {
    const data = event.data;
    emit('submit', {
        title: data.title,
        initialCost: data.initialCost,
        expectedLifespanMonth: data.expectedLifespanMonth,
        acquisitionDate: acquisitionDate.value,
        residualValue: data?.residualValue
    }, provision);

    form.title = undefined
    form.initialCost = undefined
    form.expectedLifespanMonth = undefined
    form.acquisitionDate = undefined
    form.residualValue = undefined

    emit('close', true)
}
</script>

<template>
    <UModal title="Edit Disposition d'objet">
        <template #body>
            <UForm :state="form" :validate="validate" @submit="onSubmit" class=" space-y-4">
                <UFormField label="Titre*" name="title">
                    <UInput v-model="form.title" />
                </UFormField>

                <UFormField label="Cout initial" name="initialCost">
                    <UInput 
                        type="number"
                        v-model="form.initialCost"  />
                </UFormField>

                <UFormField label="Temps de vie en mois" name="expectedLifespanMonth">
                    <UInput 
                        type="number"
                        v-model="form.expectedLifespanMonth"  />
                </UFormField>

                <UFormField label="Valeur residuelle" name="residualValue">
                    <UInput 
                        type="number"
                        v-model="form.residualValue"  />
                </UFormField>

                <UFormField label="Date d'acquistion" name="acquisitionDate">
                    <UPopover>
                        <UButton color="neutral" variant="subtle" icon="i-lucide-calendar" >
                            {{ acquisitionDate ? df.format(acquisitionDate.toDate(getLocalTimeZone()))  : 'Selectionnez une date' }}
                        </UButton>
                        <template #content>
                            <UCalendar v-model="acquisitionDate" />
                        </template>
                    </UPopover>
                </UFormField>

                <UFormField>
                    <UButton label="Submit" type="submit"/>
                </UFormField>
            </UForm>
        </template>
    </UModal> 
</template>