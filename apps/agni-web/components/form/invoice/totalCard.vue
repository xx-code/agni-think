<script lang="ts" setup>
import type { EditInvoiceDeductionType, EditTransactionType } from '~/types/form/invoice';
import type { DeductionType } from '~/types/ui/deduction';

const { deductions } = defineProps<{
    deductions: DeductionType[]
}>() 

const model = defineModel<Partial<{
    type: string
    transactions: EditTransactionType[]
    deductions: EditInvoiceDeductionType[]
}>>()

const subTotal = computed(() => {
    if (!model.value) return 0

    return model.value.transactions?.reduce((sum, trans) => sum + (trans.amount || 0), 0) || 0;
});

const deductionsTotal = computed(() => {
    if (!model.value) return 0

    if (!model.value.deductions || model.value.deductions.length === 0) return 0;
    
    let total = 0;
    
    for (const deduction of model.value.deductions) {
        const deductionType = deductions.find(i => i.id == deduction.deductionId)
        if (!deductionType) continue;
        
        let calculatedAmount = 0;
        const baseAmount = deductionType.base.toLowerCase() === 'subtotal' ? subTotal.value : subTotal.value; // On utilisera Total après pour les déductions cumulatives
        
        if (deductionType.mode.toLowerCase() === 'flat') {
            // Montant fixe
            calculatedAmount = deduction.amount || 0;
        } else if (deductionType.mode.toLowerCase() === 'rate') {
            // Pourcentage
            calculatedAmount = (baseAmount * (deduction.amount || 0)) / 100;
        }
        
        total += calculatedAmount;
    }
    
    return total;
});

const totalWithDeductions = computed(() => {
    return subTotal.value + deductionsTotal.value;
});

</script>

<template>
    <div v-if="model" class="bg-gray-50 dark:bg-gray-800 rounded-lg p-4 space-y-3">
        <div class="flex justify-between items-center">
            <span class="text-sm text-gray-600 dark:text-gray-400">
                Sous-total
            </span>
            <span class="text-lg font-semibold text-gray-900 dark:text-white">
                {{ new Intl.NumberFormat('fr-CA', { 
                    style: 'currency', 
                    currency: 'CAD' 
                }).format(subTotal) }}
            </span>
        </div>

        <div v-if="deductionsTotal > 0" class="flex justify-between items-center text-orange-600">
            <span class="text-sm">
                Déductions
            </span>
            <span class="text-lg font-semibold">
                - {{ new Intl.NumberFormat('fr-CA', { 
                    style: 'currency', 
                    currency: 'CAD' 
                }).format(deductionsTotal) }}
            </span>
        </div>

        <div class="border-t border-gray-200 dark:border-gray-700 pt-3">
            <div class="flex justify-between items-center">
                <span class="text-lg font-semibold text-gray-900 dark:text-white">
                    Total
                </span>
                <span 
                    class="text-2xl font-bold"
                    :class="model.type !== 'Income' ? 'text-red-600' : 'text-green-600'"
                >
                    {{ new Intl.NumberFormat('fr-CA', { 
                        style: 'currency', 
                        currency: 'CAD' 
                    }).format(totalWithDeductions) }}
                </span>
            </div>
            <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
                {{ model.transactions?.length || 0 }} article{{ (model.transactions?.length || 0) > 1 ? 's' : '' }}
                <span v-if="model.deductions && model.deductions.length > 0">
                    · {{ model.deductions.length }} déduction{{ model.deductions.length > 1 ? 's' : '' }}
                </span>
            </p>
        </div>
    </div>
</template>