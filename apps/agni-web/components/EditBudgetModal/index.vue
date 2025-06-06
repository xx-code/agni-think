<script setup lang="ts">
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'
import * as z from 'zod';
import { reactive, shallowRef } from "vue";
import { fetchCreateBudget, fetchUpdateBudget, useFetchBudget, useFetchListBudget } from '../../composables/budgets';
import type { FormSubmitEvent } from '@nuxt/ui';

const schema = z.object({
    title: z.string().nonempty("Vous devez ajouter un titre"),
    target: z.number().min(1, "Vous devez ajouter une valeur superieux a zero"),
    period: z.string().nonempty("Vous devez selectionner une period"),
    periodTime: z.number().min(1, "Vous devez selectionner un nombre de periode")
})

const props = defineProps({
    isEdit: Boolean,
    budgetId: String,
    onSaved: Function
    // title: String,
    // target: Number,
    // periodTime: Number,
    // period: String,
    // startDate: Date,
    // endDate: Date
})

const listPeriods = await useFetchListBudget()
let budget = null
if (props.budgetId)
    budget = await useFetchBudget(props.budgetId)

const form = reactive({
    title: budget ? budget.value?.title ?? '' : '',
    target: budget ? budget.value?.target ?? 0 : 0,
    period: budget ? budget.value?.period ?? '' : listPeriods.value[0].id,
    periodTime: budget ? budget.value?.periodTime ?? 0 : 0,
    hasEndDate: budget ? budget.value?.dateEnd !== null : false
})

let valStartDate = new Date()
if (budget)
    valStartDate = new Date(budget.value?.dateStart ?? '')

let valEndDate = new Date()
if (budget && budget.value?.dateEnd !== null)
    valEndDate = new Date(budget.value?.dateEnd ?? '') 

console.log(valEndDate)

const startDate = shallowRef(new CalendarDate(valStartDate.getFullYear(), valStartDate.getMonth() + 1, valStartDate.getDate()))
const endDate = shallowRef(new CalendarDate(valEndDate.getFullYear(), valEndDate.getMonth() + 1, valEndDate.getDate()))

const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
})

type Schema = z.output<typeof schema>

const emit = defineEmits(['close'])

async function onSubmit(event: FormSubmitEvent<Schema>) {
    if (!props.isEdit) {
        await fetchCreateBudget({
            title: form.title,
            target: form.target,
            period: form.period, 
            dateStart: startDate.value.toString(),
            dateEnd: form.hasEndDate ? endDate.value.toString() : null,
            periodTime: form.periodTime
        });
    }
    else {
        await fetchUpdateBudget({
            budgetId: props.budgetId ?? '',
            title: form.title,
            target: form.target,
            period: form.period, 
            dateStart: startDate.value.toString(),
            dateEnd: form.hasEndDate ? endDate.value.toString() : null,
            periodTime: form.periodTime
        });
    }

    form.periodTime = 0
    form.title = ''
    form.target = 0
    form.period = listPeriods.value[0].id
    form.periodTime = 0

    
    if (props.onSaved) props.onSaved()
    
    emit('close')
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
                    <USelect v-model="form.period" value-key="id" :items="listPeriods"/>
                </UFormField>

                <UFormField label="Nombre de temps" name="periodTime">
                    <UInput v-model="form.periodTime" type="number" />
                </UFormField>

                <UFormField >
                    <UButton :label="isEdit ? 'Modifier Budget' : 'Ajouter budget'" type="submit" />
                </UFormField>
            </UForm>
        </template>
    </UModal>
</template>