<script setup lang="ts">
import { reactive } from "vue";
import type { FormError, FormSubmitEvent } from '@nuxt/ui';
import type { EditIncomeSourceType, IncomeSourceType } from '~/types/ui/incomeSource';
import { fetchIncomeSourceFrequencyTypes, fetchIncomeSourceTypes } from '~/composables/internal';
import { fetchAccounts } from '~/composables/accounts/useAccounts';
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date';

const { incomeSource } = defineProps<{
    incomeSource?: IncomeSourceType
}>();

const emit = defineEmits<{
    (e: 'submit', value: EditIncomeSourceType, oldValue?: IncomeSourceType): void    
    (e: 'close', close: boolean): void
}>();

const { data: utils } = useAsyncData('principle-types', async () => {
    const res = await Promise.all([
        fetchIncomeSourceFrequencyTypes(),
        fetchIncomeSourceTypes(),
        fetchAccounts({ offset:0, limit: 0, queryAll: true})
    ])

    return {
        incomeSourceFrequencyTypes: res[0],
        incomeSourceTypes: res[1],
        accounts: res[2]
    }
})

function validate(state: Partial<EditIncomeSourceType>): FormError[] {
    const errors = []

    if (!state.title) errors.push({ name: 'title', message: 'Required' })
    if (!state.type) errors.push({ name: 'type', message: 'Required' })
    if (!state.payFrequencyType) errors.push({ name: 'payFrequencyType', message: 'Required' })
    
    if (!state.reliabilityLevel) errors.push({ name: 'reliabilityLevel', message: 'Required' })
    if (state.reliabilityLevel !== undefined) {
        if (state.reliabilityLevel < 1 || state.reliabilityLevel > 100)
            errors.push({ name: 'reliabilityLevel', message: 'reliability level must be between 1 to 100' })
    }

    if (!startDate.value) errors.push({ name: 'startDate', message: 'Required' })

    if (state.annualGrossAmount !== undefined) 
        if (state.annualGrossAmount < 0)
            errors.push({ name: 'annualGrossAmount', message: 'Annual gross amount' })

    if (!state.taxRate) errors.push({ name: 'taxRate', message: 'Required' })
    if (state.taxRate !== undefined) {
        if (state.taxRate < 0)
            errors.push({ name: 'taxRate', message: 'Tax rate must be positive' })
    }

    if (!state.otherRate) errors.push({ name: 'otherRate', message: 'Required' })
    if (state.otherRate !== undefined) {
        if (state.otherRate < 0)
            errors.push({ name: 'otherRate', message: 'Other rate must be positive' })
    }

  return errors
}

let rawStartDate = incomeSource ? incomeSource.startDate : new Date()
const startDate = shallowRef(new CalendarDate(rawStartDate.getFullYear(), rawStartDate.getMonth() + 1, rawStartDate.getDate()))

let rawEndDate = incomeSource ? incomeSource.endDate : undefined
const endDate = rawEndDate !== undefined ? shallowRef(new CalendarDate(rawEndDate.getFullYear(), rawEndDate.getMonth() + 1, rawEndDate.getDate())) : shallowRef()

const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
});


const form = reactive<Partial<EditIncomeSourceType>>({
    title: incomeSource?.title,
    otherRate: incomeSource?.otherRate,
    payFrequencyType: incomeSource?.payFrequencyType,
    reliabilityLevel: incomeSource?.reliabilityLevel,
    taxRate: incomeSource?.taxRate,
    type: incomeSource?.type,
    annualGrossAmount: incomeSource?.annualGrossAmount,
    linkedAccountId: incomeSource?.linkedAccountId
});

async function onSubmit(event: FormSubmitEvent<EditIncomeSourceType>) {
    const data = event.data;
    emit('submit', {
        title: data.title, 
        type: data.type,
        otherRate: data.otherRate,
        taxRate: data.taxRate,
        payFrequencyType: data.payFrequencyType,
        reliabilityLevel: data.reliabilityLevel,
        startDate: startDate.value,
        annualGrossAmount: data.annualGrossAmount,
        endDate: endDate.value,
        linkedAccountId: data.linkedAccountId
    }, incomeSource);


    emit('close', true)
}
</script>

<template>
    <UModal title="Edit Source de revenu">
        <template #body>
            <UForm :validate="validate" :state="form" @submit="onSubmit" class=" space-y-4">
                <UFormField label="Titre" name="title">
                    <UInput v-model="form.title" />
                </UFormField>

                <UFormField label="Taxe" name="taxRate">
                    <UInput 
                        type="number"
                        v-model="form.taxRate"  />
                </UFormField>

                <UFormField label="Autre deduction" name="otherRate">
                    <UInput 
                        type="number"
                        v-model="form.otherRate"  />
                </UFormField>

                <UFormField label="Niveau Fiabilite (1-100)" name="reliabilityLevel">
                    <UInput 
                        type="number"
                        v-model="form.reliabilityLevel"  />
                </UFormField>

                <UFormField label="Brut annuel" name="annualGrossAmount">
                    <UInput
                        type="number" 
                        v-model="form.annualGrossAmount" />
                </UFormField>

                <UFormField label="Date de debut" name="startDate">
                    <UPopover>
                        <UButton color="neutral" variant="subtle" icon="i-lucide-calendar" >
                            {{ startDate ? df.format(startDate.toDate(getLocalTimeZone()))  : 'Selectionnez une de debut' }}
                        </UButton>
                        <template #content>
                            <UCalendar v-model="startDate" />
                        </template>
                    </UPopover>
                </UFormField>

                <UFormField label="Date d'echeance" name="dueDate">
                    <UPopover>
                        <UButton color="neutral" variant="subtle" icon="i-lucide-calendar" >
                            {{ endDate ? df.format(endDate.toDate(getLocalTimeZone()))  : 'Selectionnez date de fin' }}
                        </UButton>
                        <template #content>
                            <UCalendar v-model="endDate" />
                        </template>
                    </UPopover>
                </UFormField>

                <UFormField label="Frequence de payment" name="payFrequencyType">
                    <USelect
                        v-if="utils"
                        class="w-full"
                        :items="utils.incomeSourceFrequencyTypes"
                        label-key="value"
                        value-key="id"
                        v-model="form.payFrequencyType" />
                </UFormField>

                <UFormField label="Type de revenu" name="type">
                    <USelect
                        v-if="utils"
                        class="w-full"
                        :items="utils.incomeSourceTypes"
                        label-key="value"
                        value-key="id"
                        v-model="form.type" />
                </UFormField>

                <UFormField label="Compte" name="linkedAccountId" >
                    <USelect
                        v-if="utils"
                        class="w-full"
                        :items="utils.accounts.items.map(i => ({ id: i.id, value: i.title}))"
                        label-key="value"
                        value-key="id"
                        v-model="form.linkedAccountId" />
                </UFormField>
               
                <UFormField>
                    <UButton label="Submit" type="submit"/>
                </UFormField>
            </UForm>
        </template>
    </UModal> 
</template>