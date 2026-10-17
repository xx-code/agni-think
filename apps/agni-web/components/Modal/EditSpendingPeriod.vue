<script setup lang="ts">
import type { FormError, FormSubmitEvent } from '@nuxt/ui';
import { API_ROUTES } from '~/shared/routes';
import type { UpdateSpendingPeriodRequest } from '~/types/api/spending-period';
import { SpendingPeriodStateType } from '~/types/constants/spendingPeriod';
import type { BudgetType } from '~/types/ui/budget';
import type { ScheduleInvoice } from '~/types/ui/scheduleTransaction';
import type { FormSpendingPeriod, SpendingPeriod } from '~/types/ui/spending-period';

const { period } = defineProps<{
    period: SpendingPeriod
    budgets: BudgetType[]
    scheduleInvoices: ScheduleInvoice[]
}>();

const emit = defineEmits<{
    (e: 'close', refresh: boolean): void
}>();

const toast = useToast()
const isLoading = ref(false)

const form = reactive<FormSpendingPeriod>({
    savingRateTarget: period.savingRateTarget,
    scheduleIncomesSelected: period.scheduleIncomesSelected ?? [],
    scheduleExpensesSelected: period.scheduleExpensesSelected ?? [],
    wishlistItemSelected: period.wishlistItems ?? [],
    budgetsSelected: period.budgetsSelected ?? []
})

function validate(): FormError[] {
    const errors = []

    if (form.savingRateTarget < 0 || form.savingRateTarget > 100)
        errors.push({ name: 'savingRateTarget', message: 'Le taux doit etre entre 0 et 100' })

    return errors
}


async function onSubmit(event: FormSubmitEvent<typeof form>) {
    const data = event.data

    const body: UpdateSpendingPeriodRequest = {
        savingRateTarget: data.savingRateTarget,
        scheduleExpensesSelected: data.scheduleExpensesSelected,
        scheduleIncomesSelected: data.scheduleIncomesSelected,
        state: SpendingPeriodStateType.InProgress,
        wishlistItemSelected: data.wishlistItemSelected 
    }



    try {
        isLoading.value = true

        await ApiLinkBuilder
            .route(API_ROUTES.SPENDING_PERIOD.UPDATE_SPENDING_PERIOD)
            .params({ id: period.id })
            .body(body)
            .execute()

        toast.add({
            title: 'Periode en cours',
            description: 'La periode est desormais suivie, elle peut etre terminee ou supprimee.',
            color: 'success'
        })

        emit('close', true)
    } catch(err: any) {
        toast.add({
            title: "Error spending period",
            description: err?.message,
            color: 'error'
        });
    } finally {
        isLoading.value = false
    }
}
</script>

<template>
    <UModal title="Mettre la periode en cours">
        <template #body>
            <UForm :validate="validate" :state="form" @submit="onSubmit" class="space-y-4">
                <div class="grid grid-cols-2 gap-3">
                    <UFormField label="Revenus attendus" name="scheduleIncomesSelected">
                        <UInputMenu 
                            multiple
                            :items="scheduleInvoices.filter(i => i.type === 'Income').map(i => ({ label: i.name, value: i.id}))"
                            value-key="value"
                            v-model="form.scheduleIncomesSelected" 
                        />
                    </UFormField>

                    <UFormField label="Depenses attendues" name="scheduleExpensesSelected">
                        <UInputMenu 
                            multiple
                            :items="scheduleInvoices.filter(i => i.type !== 'Income').map(i => ({ label: i.name, value: i.id}))"
                            value-key="value"
                            v-model="form.scheduleExpensesSelected" 
                        />
                    </UFormField>

                    <UFormField label="Selectionnez les budgets" name="budgetsSelected">
                        <UInputMenu 
                            multiple
                            :items="budgets.map(i => ({ label: i.title, value: i.id}))"
                            value-key="value"
                            v-model="form.budgetsSelected"
                        />
                    </UFormField>

                    <UFormField label="Achats souhaites" name="wishtlistItems">
                        <UInputMenu 
                            multiple
                            v-model="form.wishlistItemSelected"
                        />
                    </UFormField>

                    <UFormField label="Taux d'epargne cible (%)" name="savingRateTarget">
                        <UInput
                            type="number"
                            max="100"
                            step="0.01"
                            v-model="form.savingRateTarget" />
                    </UFormField>
                </div>

                <UFormField>
                    <div class="flex justify-end w-full">
                        <UButton label="Mettre en cours" type="submit" :loading="isLoading" />
                    </div>
                </UFormField>
            </UForm>
        </template>
    </UModal>
</template>
