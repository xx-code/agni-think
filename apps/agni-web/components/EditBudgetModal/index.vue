<script setup lang="ts">
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'
import * as z from 'zod';
import { reactive, shallowRef } from "vue";
import { useListPeriodBudget } from '../../composables/budgets';
import type { FormSubmitEvent } from '@nuxt/ui';

const schema = z.object({
    title: z.string().nonempty("Vous devez ajouter un titre"),
    target: z.number().min(1, "Vous devez ajouter une valeur superieux a zero"),
    period: z.string().nonempty("Vous devez selectionner une period"),
    periodTime: z.number().min(1, "Vous devez selectionner un nombre de periode")
})

const props = defineProps({
    isEdit: Boolean,
    title: String,
    target: Number,
    periodTime: Number,
    period: String,
    startDate: Date,
    endDate: Date
})

const listPeriods = useListPeriodBudget()

const form = reactive({
    title: props.title,
    target: props.target ? props.target : 0,
    period: props.period ? props.period : listPeriods.value[0].value,
    periodTime: props.periodTime,
    hasEndDate: false
})

const startDate = shallowRef(new CalendarDate(new Date().getFullYear(), new Date().getMonth() + 1, new Date().getDate()))
const endDate = shallowRef(new CalendarDate(new Date().getFullYear(), new Date().getMonth() + 1, new Date().getDate()))
const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
})

type Schema = z.output<typeof schema>

function onSubmit(event: FormSubmitEvent<Schema>) {
    console.log(form)
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
                            {{ endDate ? df.format(startDate.toDate(getLocalTimeZone())) : 'Selectionnez une de fin' }}
                        </UButton>
                        <template #content>
                            <UCalendar v-model="endDate" />
                        </template>
                    </UPopover>
                </UFormField>

                <UFormField label="Periode" name="period">
                    <USelect v-model="form.period" value-key="value" label-key="title" :items="listPeriods"/>
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