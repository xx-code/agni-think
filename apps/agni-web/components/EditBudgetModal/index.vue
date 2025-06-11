<script setup lang="ts">
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'
import * as z from 'zod';
import { reactive, ref, shallowRef, watchEffect, type Ref } from "vue";
import { fetchCreateBudget, fetchUpdateBudget, useFetchBudget, useFetchListPeriod } from '../../composables/budgets';
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
})

const {data: listPeriods, error, refresh} = useFetchListPeriod()
let budget = null
if (props.budgetId !== undefined) {
    const {data} = useFetchBudget(props.budgetId)
    budget = data
}


const form = reactive({
    title: '',
    target: 0,
    period: '',
    periodTime: 0,
    hasEndDate: false
})

let today = new Date()

const startDate = shallowRef(new CalendarDate(today.getFullYear(), today.getMonth() + 1, today.getDate()))
const endDate = shallowRef(new CalendarDate(today.getFullYear(), today.getMonth() + 1, today.getDate()))

const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
})

type Schema = z.output<typeof schema>

const emit = defineEmits(['close'])

watchEffect(() => {
    let defPeriod = ''
    if (listPeriods.value?.length > 0)
        defPeriod = listPeriods.value[0].id 

    if (budget?.value) {
        form.title = budget.value.title
        form.period = budget.value.period? budget.value.period : defPeriod
        form.target = budget.value.target
        form.periodTime = budget.value.periodTime
        form.hasEndDate = budget.value.dateEnd !== null 
        const valStartDate = new Date(budget.value.dateStart ?? '')
        startDate.value = new CalendarDate(valStartDate.getFullYear(), valStartDate.getMonth() + 1, valStartDate.getDate())

        if  (budget.value.dateEnd) {
            const valEndDate = new Date(budget.value.dateEnd ?? '')
            endDate.value = new CalendarDate(valEndDate.getFullYear(), valEndDate.getMonth() + 1, valEndDate.getDate())
        }
    } 
})

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