<script setup lang="ts">
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'
import { reactive, shallowRef, ref } from "vue";
import type { FormError, FormSubmitEvent } from '@nuxt/ui';
import usePeriodTypes from '~/composables/internals/usePeriodTypes';
import type { BudgetType, EditBudgetType } from '~/types/ui/budget';
import useSaveGoals from '~/composables/goals/useSaveGoals';

const { budget } = defineProps<{
    budget?: BudgetType
}>();

const emit = defineEmits<{
    (e: 'submit', value: EditBudgetType, oldValue?: BudgetType): void    
    (e: 'close', close: boolean): void
}>();


const { data:saveGoals } = useSaveGoals({ offset: 0, limit: 0, queryAll: true})

const {data: listPeriods, error, refresh} = usePeriodTypes();

const form = reactive<Partial<EditBudgetType>>({
    title: budget?.title || '',
    target: budget?.target || 0,
    saveGoalIds: [],
    repeater: budget?.repeater,

})
const saveGoalIds = ref(budget?.saveGoalIds || [])
const isRecurrence = ref(budget?.repeater !== undefined)
function onChangeIsRecurrence(isRecurrence: boolean) {
    form.repeater = isRecurrence ? (budget?.repeater || { period: "Day", interval: 1}) : undefined
}

let rawDueDate = budget ? budget.dueDate : new Date();
const dueDate = shallowRef(new CalendarDate(rawDueDate.getFullYear(), rawDueDate.getMonth() + 1, rawDueDate.getDate()));

const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
})

function validate(state: Partial<EditBudgetType>): FormError[] {
  const errors = []

  if (!state.title) errors.push({ name: 'title', message: 'Required' })
  if (!state.target) errors.push({ name: 'target', message: 'Required' })
  if (!dueDate.value) errors.push({ name: 'dueDate', message: 'Required' })
  if (state.repeater && !state.repeater.period) errors.push({ name: 'period', message: 'Required' })
  if (state.repeater && !state.repeater.interval) errors.push({ name: 'interval', message: 'Required' })

  return errors
}

async function onSubmit(event: FormSubmitEvent<EditBudgetType>) {
    const data = event.data;
    emit('submit', {
        title: data.title,
        target: data.target,
        saveGoalIds: saveGoalIds.value,
        dueDate: dueDate.value, 
        repeater: data.repeater
    }, budget);

    form.title = "";
    form.target = 0;
    form.dueDate = undefined;
    form.repeater = undefined;
    
    emit('close', true);
}
</script>

<template>
    <UModal title="Editeur de budget">
        <template #body>
            <UForm :validate="validate" :state="form" @submit="onSubmit" class="space-y-4">
                <UFormField label="Titre" name="title">
                    <UInput v-model="form.title" />
                </UFormField>

                <UFormField label="Somme" name="target">
                    <UInput v-model="form.target" type="number"/>
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

                <UFormField label="Nombre de temps" name="interval" v-if="form.repeater">
                    <UInput v-model="form.repeater.interval" type="number" />
                </UFormField>

                <UFormField label="But d'epargne relie" >
                    <UInputMenu multiple v-model="saveGoalIds" value-key="value" 
                        :items="saveGoals?.items.map(i => ({ label: i.title, value: i.id }))" />
                </UFormField>

                <UFormField >
                    <UButton :label="budget ? 'Modifier Budget' : 'Ajouter budget'" type="submit" />
                </UFormField>
            </UForm>
        </template>
    </UModal>
</template>
