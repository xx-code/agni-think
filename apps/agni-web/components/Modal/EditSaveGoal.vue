<script setup lang="ts">
import * as z from 'zod'
import { reactive } from "vue";
import type { FormSubmitEvent } from '@nuxt/ui';
import type { EditSaveGoalType, SaveGoalType } from '~/types/ui/saveGoal';
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date';
import useIntensityDesirTypes from '~/composables/internals/useIntensityDerisTypes';
import useImportanceTypes from '~/composables/internals/useImportanceTypes';
import { parseAsLocalDate } from '~/utils/parseAsLocalDate';

const { saveGoal } = defineProps<{
    saveGoal?: SaveGoalType
}>();
const emit = defineEmits<{
    (e: 'submit', value: EditSaveGoalType, oldValue?: SaveGoalType): void    
    (e: 'close', close: boolean): void
}>();
const schema = z.object({
    title: z.string().nonempty('Vous devez ajouter un titre'),
    description: z.string().optional(),
    targetAmount: z.number().gt(0, 'La somme d\'argent doit etre superieux a zero'),
    desirValue: z.any(),
    importance: z.any(),
    wishDueDate: z.date().optional()
})

type Schema = z.output<typeof schema>;

const { data:intensityDesir } = useIntensityDesirTypes();
const { data:importances } = useImportanceTypes();

const form = reactive({
    title: saveGoal?.title || '',
    description: saveGoal?.description || '',
    targetAmount: saveGoal?.target || 0,
    desirValue: saveGoal?.desirValue,
    importance: saveGoal?.importance,
    hasWishDueDate: saveGoal?.wishDueDate ? true : false
});

const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
});

let wishDated: Date|undefined; 
if (saveGoal?.wishDueDate)
    wishDated = parseAsLocalDate(saveGoal.wishDueDate);
const wishDate = shallowRef(wishDated ? new CalendarDate(wishDated.getFullYear(), wishDated.getMonth() + 1, wishDated.getDate()) : undefined);

async function onSubmit(event: FormSubmitEvent<Schema>) {
    const data = event.data;
    emit('submit', {
        title: data.title,
        target: data.targetAmount,
        description: data.description || '',
        desirValue: data.desirValue,
        importance: data.importance,
        wishDueDate: form.hasWishDueDate ? wishDate.value : undefined
    }, saveGoal);

    form.title = "";
    form.description = "";
    form.targetAmount = 0;

    emit('close', true);
}

</script>

<template>
    <UModal title="Etiteur de but d'epargne" >
        <template #body>
            <UForm :schema="schema" :state="form" @submit="onSubmit" class="space-y-4">
                <UFormField label="Nom du but d'epargne" name="title">
                    <UInput v-model="form.title" class="w-full" />
                </UFormField>
                <UFormField label="Petit description d'epargne" name="description">
                    <UInput v-model="form.description" class="w-full" />
                </UFormField>
                <UFormField label="Someme du but d'epargne" name="targetAmount">
                    <UInput v-model="form.targetAmount" class="w-full" type="number" />
                </UFormField>

                <UFormField label="Intensite de desire" name="desirValue">
                    <URadioGroup 
                        orientation="horizontal" 
                        variant="list" 
                        :items="intensityDesir || []" 
                        value-key="id"
                        label-key="value"
                        v-model="form.desirValue" />
                </UFormField>

                <UFormField label="Importance" name="importance">
                    <URadioGroup 
                        orientation="horizontal" 
                        variant="list" 
                        value-key="id"
                        label-key="value"
                        v-model="form.importance"
                        :items="importances || []" />
                </UFormField> 

                <UFormField label="Date butoir desirer" name="hasWishDueDate">
                    <USwitch v-model="form.hasWishDueDate" />
                </UFormField>

                <UFormField label="" name="wishDueDate" v-if="form.hasWishDueDate">
                    <UPopover>
                        <UButton color="neutral" variant="subtle" icon="i-lucide-calendar" >
                            {{ wishDate ? df.format(wishDate.toDate(getLocalTimeZone())) : 'Selectionnez une de fin' }}
                        </UButton>
                        <template #content>
                            <UCalendar v-model="wishDate" />
                        </template>
                    </UPopover>
                </UFormField>
                <UButton label="Submit" type="submit" />
            </UForm>
        </template>
    </UModal>
</template>