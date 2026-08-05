<script setup lang="ts">
import * as z from 'zod'
import { reactive } from "vue";
import type { FormSubmitEvent } from '@nuxt/ui';
import type { EditFundType, FundType } from '~/types/ui/fund';
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date';
import { fetchAccounts } from '~/composables/api/accounts';
import { fetchImportanceTypes, fetcheIntensityDesirTypes } from '~/composables/api/internal';

const { saveGoal } = defineProps<{
    saveGoal?: FundType
}>();
const emit = defineEmits<{
    (e: 'submit', value: EditFundType, oldValue?: FundType): void    
    (e: 'close', close: boolean): void
}>();
const schema = z.object({
    title: z.string().nonempty('Vous devez ajouter un titre'),
    description: z.string().optional(),
    accountId: z.string().optional(),
    targetAmount: z.number().gt(0, 'La somme d\'argent doit etre superieux a zero'),
    desirValue: z.any(),
    importance: z.any(),
    wishDueDate: z.date().optional()
})

type Schema = z.output<typeof schema>;

const { data: utils } = useAsyncData('utils+accounts', async () => {
    const query = { offset: 0, limit: 0, queryAll: true }
    const [ accounts, importances, intensityDesirs ] = await Promise.all([
        fetchAccounts(query),
        fetchImportanceTypes(),
        fetcheIntensityDesirTypes()
    ])

    return {
        accounts,
        importances,
        intensityDesirs
    }
})

const form = reactive({
    title: saveGoal?.title || '',
    accountId: saveGoal?.accountId || '',
    description: saveGoal?.description || '',
    targetAmount: saveGoal?.target || 0,
});

async function onSubmit(event: FormSubmitEvent<Schema>) {
    const data = event.data;
    emit('submit', {
        title: data.title,
        target: data.targetAmount,
        accountId: data.accountId,
        description: data.description || ''
    }, saveGoal);

    form.title = "";
    form.description = "";
    form.targetAmount = 0;

    emit('close', true);
}

</script>

<template>
    <UModal :title="saveGoal ? saveGoal?.title : 'Etiteur de but d\'epargne '" >
        <template #body>
            <UForm :schema="schema" :state="form" @submit="onSubmit" class="space-y-4">
                <UFormField label="Nom du but d'epargne" name="title">
                    <UInput v-model="form.title" class="w-full" />
                </UFormField>
                <UFormField label="Petit description d'epargne" name="description">
                    <UTextarea v-model="form.description" autoresize class="w-full" />
                </UFormField>
                <UFormField label="Someme du but d'epargne" name="targetAmount">
                    <UInput v-model="form.targetAmount" class="w-full" type="number" />
                </UFormField>


                <UFormField label="Compte" name="accountId" >
                    <USelect 
                        v-model="form.accountId" 
                        value-key="value" 
                        :items="utils?.accounts.items.map(i => ({value: i.id, label: i.title}))" 
                        class="w-full" />
                </UFormField>

                <UButton label="Submit" type="submit" />
            </UForm>
        </template>
    </UModal>
</template>