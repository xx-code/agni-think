<script setup lang="ts">
import type { FormError, FormSubmitEvent } from '@nuxt/ui';
import type { EditInvoiceType } from '~/types/form/invoice';
import type { AccountType } from '~/types/ui/account';
import type { BudgetType } from '~/types/ui/budget';
import type { CategoryType } from '~/types/ui/category';
import type { DeductionType } from '~/types/ui/deduction';
import type { TagType } from '~/types/ui/tag';

const { accounts, invoiceTypes, 
        deductions, categories, tags,
        validate, initSwitchMore = false } = defineProps<{
    isUpdate: boolean
    accounts: AccountType[]
    categories: CategoryType[]
    budgets: BudgetType[]
    tags: TagType[]
    deductions: DeductionType[]
    invoiceTypes: {label: string, value: string}[]
    initSwitchMore: boolean
    validate: (state: Partial<EditInvoiceType>) => FormError[]
}>()

const model = defineModel<Partial<EditInvoiceType>>()

const emit = defineEmits<{
    submit: [event: FormSubmitEvent<EditInvoiceType>],
    switchMore: [state: boolean] 
    close: []
}>()

</script>

<template>
    <UForm :validate="validate" :state="model" class="space-y-6" @submit="e => emit('submit', e)">
        <FormInvoiceHeader 
            :accounts="accounts"
            :invoice-types="invoiceTypes"

            v-model="model"
        />

        <FormInvoiceTransactionList 
            v-model="model"
            :budgets="budgets"
            :categories="categories"
            :tags="tags"
        />

        <FormInvoiceDeductionList 
            v-model="model"
            :deductions="deductions"
        />

        <FormInvoiceTotalCard 
            :deductions="deductions"
            v-model="model"
        />

        <div class="flex justify-between items-center border-t pt-4 border-neutral-200">
            <USwitch 
                v-if="!isUpdate" 
                label="Ajouter plus" 
                :default-value="initSwitchMore"
                @update:model-value="state => emit('switchMore', state)" />

            <div class="flex justify-end gap-3">
                <UButton 
                    label="Annuler" 
                    color="neutral"
                    variant="outline"
                    size="lg"
                    @click="emit('close')" 
                />
                <UButton 
                    type="submit"
                    :label="isUpdate ? 'Mettre à jour' : 'Créer'"
                    icon="i-lucide-check"
                    size="lg"
                />
            </div>
        </div> 
    </UForm>
</template>