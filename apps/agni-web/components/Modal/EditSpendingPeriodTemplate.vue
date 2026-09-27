<script setup lang="ts">
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date';
import type { FormError, FormSubmitEvent } from '@nuxt/ui';
import { listBudgetsResponseToListBudgets } from '~/mappers/budget';
import { API_ROUTES } from '~/shared/routes';
import type { CreatedRequest, ListResponse, QueryFilterRequest } from '~/types/api';
import type { GetBudgetResponse } from '~/types/api/budget';
import type { GetInternalTypeResponse } from '~/types/api/internal';
import type { CreateSpendingPeriodTemplateRequest, UpdateSpendingPeriodTemplateRequest } from '~/types/api/spending-period-template';
import type { SpendingPeriodTemplate } from '~/types/ui/spending-period-template';

const { spendingPeriodTemplate } = defineProps<{
    spendingPeriodTemplate?: SpendingPeriodTemplate
}>();

const emit = defineEmits<{
    (e: 'close', close: boolean): void
}>();

const toast = useToast()
const isLoading = ref(false)

const { data: utils } = useAsyncData('spending-period-template+period-types+budgets', async () => {
    const periodTypes = await ApiLinkBuilder
        .route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.PERIOD_TYPE)
        .execute()
    const budgets = await ApiLinkBuilder
        .route<ListResponse<GetBudgetResponse>>(API_ROUTES.BUDGETS.GET_BUDGETS)
        .query({ offset: 0, limit: 1, queryAll: true } as QueryFilterRequest)
        .mapper(listBudgetsResponseToListBudgets)
        .execute()
    
    return {
        periodTypes,
        budgets
    }
})

function toCalendarDate(date: Date): CalendarDate {
    return new CalendarDate(date.getFullYear(), date.getMonth() + 1, date.getDate())
}

const rawStartDate = spendingPeriodTemplate?.startDate ?? new Date()
const startDate = shallowRef(toCalendarDate(rawStartDate))

const rawEndDate = spendingPeriodTemplate?.endDate
const endDate = rawEndDate !== undefined ? shallowRef(toCalendarDate(rawEndDate)) : shallowRef()

const df = new DateFormatter('fr-CA', {
    dateStyle: 'medium'
});

const form = reactive({
    budgetIds: spendingPeriodTemplate?.budgets.map(i => i.id) ?? [],
    isActive: spendingPeriodTemplate?.isActive ?? false,
    recurrence: {
        period: spendingPeriodTemplate?.recurrence.period ?? 'Month',
        interval: spendingPeriodTemplate?.recurrence.interval ?? 1
    }
})

function validate(): FormError[] {
    const errors = []

    if (!startDate.value) errors.push({ name: 'startDate', message: 'Required' })
    if (startDate.value && endDate.value && endDate.value.compare(startDate.value) < 0)
        errors.push({ name: 'endDate', message: 'La date de fin doit etre posterieure a la date de debut' })

    if (!form.recurrence.period) errors.push({ name: 'recurrence', message: 'Required' })
    if (form.recurrence.interval < 1)
        errors.push({ name: 'recurrence', message: 'L intervalle doit etre superieur ou egal a 1' })

    return errors
}

async function onSubmit(event: FormSubmitEvent<typeof form>) {
    const data = event.data

    const recurrence = {
        period: data.recurrence.period,
        interval: data.recurrence.interval
    }

    try {
        isLoading.value = true

        if (spendingPeriodTemplate) {
            const body: UpdateSpendingPeriodTemplateRequest = {
                startDate: startDate.value?.toString(),
                endDate: endDate.value?.toString(),
                targetBudgetIds: data.budgetIds,
                isActive: data.isActive,
                recurrence
            }

            await ApiLinkBuilder
                .route(API_ROUTES.SPENDING_PERIOD_TEMPLATES.PUT_SPENDING_PERIOD_TEMPLATE)
                .params({ id: spendingPeriodTemplate.id })
                .body(body)
                .execute()
        } else {
            const body: CreateSpendingPeriodTemplateRequest = {
                startDate: startDate.value!.toString(),
                endDate: endDate.value?.toString(),
                targetBudgetIds: data.budgetIds,
                recurrence
            }

            await ApiLinkBuilder
                .route<CreatedRequest>(API_ROUTES.SPENDING_PERIOD_TEMPLATES.CREATE_SPENDING_PERIOD_TEMPLATE)
                .body(body)
                .execute()
        }

        emit('close', true)
    } catch(err: any) {
        toast.add({
            title: "Error spending period template",
            description: err?.message,
            color: 'error'
        });
    } finally {
        isLoading.value = false
    }
}
</script>

<template>
    <UModal :title="spendingPeriodTemplate ? 'Modifier le modele de periode' : 'Nouveau modele de periode'">
        <template #body>
            <UForm :validate="validate" :state="form" @submit="onSubmit" class="space-y-4">
                <UFormField label="Date de debut" name="startDate">
                    <UPopover>
                        <UButton type="button" color="neutral" variant="subtle" icon="i-lucide-calendar" class="w-full justify-start">
                            {{ startDate ? df.format(startDate.toDate(getLocalTimeZone())) : 'Selectionnez une date de debut' }}
                        </UButton>
                        <template #content>
                            <UCalendar v-model="startDate" />
                        </template>
                    </UPopover>
                </UFormField>

                <UFormField label="Date de fin" name="endDate">
                    <UPopover>
                        <UButton type="button" color="neutral" variant="subtle" icon="i-lucide-calendar" class="w-full justify-start">
                            {{ endDate ? df.format(endDate.toDate(getLocalTimeZone())) : 'Selectionnez une date de fin' }}
                        </UButton>
                        <template #content>
                            <UCalendar v-model="endDate" />
                        </template>
                    </UPopover>
                </UFormField>

                <UFormField label="Budgets" name="budget">
                    <UInputMenu 
                        v-if="utils?.budgets"
                        class="flex-1 w-full"
                        :items="utils.budgets.items.map(i => ({ value: i.id, label: i.title }))"
                        label-key="label"
                        value-key="value"
                        multiple
                        v-model="form.budgetIds" />
                </UFormField>

                <UFormField label="Recurrence" name="recurrence">
                    <div class="flex items-center gap-2">
                        <USelect
                            v-if="utils?.periodTypes"
                            class="flex-1"
                            :items="utils.periodTypes"
                            label-key="value"
                            value-key="id"
                            v-model="form.recurrence.period" />

                        <UInput
                            type="number"
                            min="1"
                            class="w-28"
                            v-model="form.recurrence.interval" />
                    </div>
                </UFormField>

                <UFormField v-if="spendingPeriodTemplate" label="Actif" name="isActive">
                    <USwitch v-model="form.isActive" />
                </UFormField>

                <UFormField>
                    <UButton label="Submit" type="submit" :loading="isLoading" />
                </UFormField>
            </UForm>
        </template>
    </UModal>
</template>
