<script setup lang="ts">
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'
import * as z from 'zod';
import { reactive, shallowRef, watchEffect } from "vue";
import type { FormSubmitEvent } from '@nuxt/ui';
import usePeriodTypes from '~/composables/internals/usePeriodTypes';
import type { BudgetType, EditBudgetType } from '~/types/ui/budget';

const { budget } = defineProps<{
    budget?: BudgetType
}>();
const emit = defineEmits<{
    (e: 'submit', value: EditBudgetType, oldValue?: BudgetType): void    
    (e: 'close', close: boolean): void
}>();

const schema = z.object({
    title: z.string().nonempty("Vous devez ajouter un titre"),
    target: z.number().min(1, "Vous devez ajouter une valeur superieux a zero"),
    period: z.string().nonempty("Vous devez selectionner une period"),
    periodTime: z.number().min(1, "Vous devez selectionner un nombre de periode")
});

const {data: listPeriods, error, refresh} = usePeriodTypes();

const form = reactive({
    title: budget?.title || '',
    target: budget?.target || 0,
    period: budget?.period || '',
    periodTime: budget?.periodTime || 0,
    hasEndDate: false
});

let startDated = budget ? new Date(budget.startDate)  : new Date();
let endDated: Date|undefined; 
if (budget?.endDate)
    endDated = new Date(budget.endDate);


const startDate = shallowRef(new CalendarDate(startDated.getFullYear(), startDated.getMonth() + 1, startDated.getDate()));
const endDate = shallowRef(endDated ? new CalendarDate(endDated.getFullYear(), endDated.getMonth() + 1, endDated.getDate()) : undefined);

const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
})

type Schema = z.output<typeof schema>


async function onSubmit(event: FormSubmitEvent<Schema>) {
    const data = event.data;
    emit('submit', {
        title: data.title,
        target: data.target,
        period: data.period,
        periodTime: data.periodTime,
        startDate: startDate.value,
        endDate: endDate.value 
    }, budget);

    form.title = "";
    form.period = "";
    form.periodTime = 0;
    form.target = 0;
    form.hasEndDate = false;
    
    emit('close', true);
}
</script>

<template>
    <UModal title="Editeur de budget">
        <template #body>
            <UForm :schema="schema" :state="form" @submit="onSubmit" class="space-y-4">
                <UFormField label="Titre" name="title">
                    <UInput v-model="form.title" />
                </UFormField>

                <UFormField label="Somme" name="target">
                    <UInput v-model="form.target" type="number"/>
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
                    <UButton :label="budget ? 'Modifier Budget' : 'Ajouter budget'" type="submit" />
                </UFormField>
            </UForm>
        </template>
    </UModal>
</template>
