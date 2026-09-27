<script setup lang="ts">
import type { FormError, FormSubmitEvent } from '@nuxt/ui';
import { API_ROUTES } from '~/shared/routes';
import type { UpdateSpendingPeriodRequest } from '~/types/api/spending-period';
import { SpendingPeriodType } from '~/types/constants/spendingPeriod';
import type { EditSpendingPeriodType, SpendingPeriod } from '~/types/ui/spending-period';

const { period } = defineProps<{
    period: SpendingPeriod
}>();

const emit = defineEmits<{
    (e: 'close', refresh: boolean): void
}>();

const toast = useToast()
const isLoading = ref(false)

const form = reactive<EditSpendingPeriodType>({
    freeAmount: period.freeAmount,
    savingRateTarget: period.savingRateTarget,
    totalExpectedIncome: period.totalExpectedIncome,
    totalExpectedExpenses: period.totalExpectedExpenses,
    wantSpendingItems: period.wantSpendingItems.map(i => ({ ...i }))
})

function validate(): FormError[] {
    const errors = []

    // if (form.freeAmount < 0) errors.push({ name: 'freeAmount', message: 'Le montant libre doit etre positif' })
    if (form.savingRateTarget < 0 || form.savingRateTarget > 100)
        errors.push({ name: 'savingRateTarget', message: 'Le taux doit etre entre 0 et 100' })
    if (form.totalExpectedIncome < 0) errors.push({ name: 'totalExpectedIncome', message: 'Le revenu doit etre positif' })
    if (form.totalExpectedExpenses < 0) errors.push({ name: 'totalExpectedExpenses', message: 'La depense doit etre positive' })

    form.wantSpendingItems.forEach((item, index) => {
        if (!item.description) errors.push({ name: `wantSpendingItems.${index}.description`, message: 'Required' })
        if (item.amount < 0) errors.push({ name: `wantSpendingItems.${index}.amount`, message: 'Le montant doit etre positif' })
    })

    return errors
}

function addWantSpendingItem() {
    form.wantSpendingItems.push({ description: '', amount: 0 })
}

function removeWantSpendingItem(index: number) {
    form.wantSpendingItems.splice(index, 1)
}

async function onSubmit(event: FormSubmitEvent<typeof form>) {
    const data = event.data

    const body: UpdateSpendingPeriodRequest = {
        freeAmount: data.freeAmount,
        savingRateTarget: data.savingRateTarget,
        totalExpectedIncome: data.totalExpectedIncome,
        totalExpectedExpenses: data.totalExpectedExpenses,
        state: SpendingPeriodType.InProgress,
        wantSpendingItems: data.wantSpendingItems.map(i => ({
            description: i.description,
            amount: i.amount
        }))
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
                    <UFormField label="Revenus attendus" name="totalExpectedIncome">
                        <UInput
                            type="number"
                            step="0.01"
                            v-model="form.totalExpectedIncome" />
                    </UFormField>

                    <UFormField label="Depenses attendues" name="totalExpectedExpenses">
                        <UInput
                            type="number"
                            step="0.01"
                            v-model="form.totalExpectedExpenses" />
                    </UFormField>

                    <UFormField label="Montant libre" name="freeAmount">
                        <UInput
                            type="number"
                            step="0.01"
                            v-model="form.freeAmount" />
                    </UFormField>

                    <UFormField label="Taux d'epargne cible (%)" name="savingRateTarget">
                        <UInput
                            type="number"
                            max="100"
                            step="0.01"
                            v-model="form.savingRateTarget" />
                    </UFormField>
                </div>

                <UFormField label="Achats souhaites" name="wantSpendingItems">
                    <div class="w-full space-y-2">
                        <div
                            v-for="(item, index) in form.wantSpendingItems"
                            :key="index"
                            class="flex items-center gap-2">
                            <UInput
                                class="flex-1"
                                placeholder="Description"
                                v-model="item.description" />
                            <UInput
                                class="w-32"
                                type="number"
                                min="0"
                                step="0.01"
                                v-model="item.amount" />
                            <UButton
                                icon="i-lucide-trash-2"
                                color="error"
                                variant="ghost"
                                @click="removeWantSpendingItem(index)"
                            />
                        </div>

                        <UButton
                            type="button"
                            size="xs"
                            variant="soft"
                            color="neutral"
                            icon="i-lucide-plus"
                            label="Ajouter un achat"
                            @click="addWantSpendingItem"
                        />
                    </div>
                </UFormField>

                <UFormField>
                    <div class="flex justify-end w-full">
                        <UButton label="Mettre en cours" type="submit" :loading="isLoading" />
                    </div>
                </UFormField>
            </UForm>
        </template>
    </UModal>
</template>
