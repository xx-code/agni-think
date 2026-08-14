<script setup lang="ts">
import { reactive } from "vue";
import { CalendarDate, getLocalTimeZone } from '@internationalized/date';
import type { FormError, FormSubmitEvent } from '@nuxt/ui';
import { createGoal, updateGoal } from '~/composables/api/goals';
import type { GoalForm } from '~/types/form/goal';
import { goalFormToCreateRequest, goalFormToUpdateRequest } from '~/mappers/goal';
import type { GoalType } from '~/types/constants/goal';

const { goalId, initData, type, targetSourceId } = defineProps<{
    goalId?: string
    initData?: GoalForm
    type: GoalType
    targetSourceId: string
}>()
const emit = defineEmits<{
    (e: 'close', goalId?: string): void
}>()
const toast = useToast()

const isloading = ref(false)
const form = reactive<Partial<GoalForm>>({
    ...initData,
    type,
    targetSourceId
})

function validate(state: Partial<GoalForm>): FormError[] {
    const errors: FormError[] = []

    if (!state.title)
        errors.push({name: 'title', message: 'Require'})

    if (state.title && state.title.trim() === "")
        errors.push({name: 'title', message: 'Vous devez ajouter un titre'})

    if (!state.targetAmount)
        errors.push({name: 'targetAmount', message: 'Require'})

    if (state.targetAmount && state.targetAmount <= 0)
        errors.push({name: 'targetAmount', message: 'Le montant dois etre superieur a zero'})

    if (!state.targetDate)
        errors.push({ name: 'targetDate', message: 'Required' })

    return errors
}

async function onSubmit(event: FormSubmitEvent<GoalForm>) {
    const data = event.data
    isloading.value = true
    try {
        let id = goalId
        if (goalId && initData)
            await updateGoal(goalId, goalFormToUpdateRequest(data))
        else {
            const res = await createGoal(goalFormToCreateRequest(data))
            id = res.newId
        }

        toast.add({
            title: 'Succès',
            description: goalId ? 'Objectif mis à jour' : 'Objectif créé',
            color: 'success'
        })

        emit('close', id)
    } catch (err: any) {
        toast.add({
            title: 'Erreur',
            description: err.message || 'Une erreur est survenue',
            color: 'error'
        })
    } finally {
        isloading.value = false
    }
}

const calendarValue = computed({
    get() {
        if (!form) return undefined

        try {
            let valDate = form.targetDate ? form.targetDate : new Date()
            const date = new CalendarDate(
                valDate.getFullYear(), 
                valDate.getMonth() + 1, 
                valDate.getDate()
            );
            form.targetDate = date.toDate(getLocalTimeZone())

            return date
        } catch {
            return undefined
        }
    },
    set(newValue: CalendarDate | undefined) {
        if (!form) return

        if (newValue) {
            let valDate = newValue.toDate(getLocalTimeZone())
            form.targetDate = valDate
        } else {

            form.targetDate = undefined
        }
    }
})
</script>

<template>
    <UModal>
        <template #body>
            <UForm :validate="validate" :state="form" @submit="onSubmit" class="space-y-4">
                <UFormField label="Titre de l'objectif" name="title">
                    <UInput v-model="form.title" class="w-full" />
                </UFormField>

                <UFormField label="Description" name="description">
                    <UTextarea v-model="form.description" autoresize class="w-full" />
                </UFormField>

                <UFormField label="Montant cible" name="targetAmount">
                    <UInput v-model="form.targetAmount" class="w-full" type="number" />
                </UFormField>

                <UFormField label="Date d'echeance" name="dueDate" >
                    <UPopover>
                        <UButton 
                            color="neutral" 
                            variant="outline" 
                            icon="i-lucide-calendar"
                            size="lg"
                            block
                        >
                            {{ calendarValue ? formatDate(calendarValue.toDate(getLocalTimeZone())) : 'Sélectionner une date' }}
                        </UButton>
                        <template #content>
                            <UCalendar v-model="calendarValue"/>
                        </template>
                    </UPopover>
                </UFormField>

                <UButton label="Submit" type="submit" />
            </UForm>
        </template> 
    </UModal>
</template>