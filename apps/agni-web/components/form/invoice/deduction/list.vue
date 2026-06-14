<script setup lang="ts">
import type { EditInvoiceDeductionType, EditTransactionType } from '~/types/form/invoice';
import type { DeductionType } from '~/types/ui/deduction';

const { deductions } = defineProps<{
    deductions: DeductionType[]
}>()

const model = defineModel<Partial<{
    type: string
    deductions: EditInvoiceDeductionType[]
    transactions: EditTransactionType[]
}>>()

const subTotal = computed(() => {
    if (!model.value) return 0

    return model.value.transactions?.reduce((sum, trans) => sum + (trans.amount || 0), 0) || 0;
})

function addDeduction() {
    if (!model.value) return 

    if (!model.value.deductions) model.value.deductions = [];
    model.value.deductions.push({
        deductionId: '',
        amount: 0
    });
}

function removeDeduction(index: number) {
    if (!model.value) return 

    if (model.value.deductions) {
        model.value.deductions.splice(index, 1);
    }
}
</script>

<template>
    <div v-if="model && model.type !== 'Income'" class="space-y-4">
        <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">
                Déductions ({{ model.deductions?.length || 0 }})
            </h3>
            <UButton 
                icon="i-lucide-plus" 
                label="Ajouter une déduction"
                color="primary"
                variant="soft"
                @click="addDeduction"
            />
        </div>


        <div v-if="model.deductions && model.deductions.length > 0" class="space-y-3">
            <FormInvoiceDeductionCard 
                v-for="(deduction, index) in model.deductions" 
                :key="index"
                :deductions="deductions"
                :index="index"
                :sub-total="subTotal"

                @remove="removeDeduction(index)"

                v-model="model.deductions[index]"
            />
        </div> 

        <div v-else class="text-center py-8 text-gray-500 dark:text-gray-400">
            <UIcon name="i-lucide-info" class="text-2xl mb-2" />
            <p class="text-sm">Aucune déduction ajoutée</p>
        </div>
    </div>
</template>