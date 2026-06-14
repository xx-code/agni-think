<script setup lang="ts">
import type { DeductionType } from '~/types/ui/deduction';

const { index, deductions, subTotal } = defineProps<{
    index: number
    deductions: DeductionType[]
    subTotal: number
}>()

const model = defineModel<{
    deductionId: string
    amount: number
}>()

const emit = defineEmits(["remove"])

function getDeductionById(id: string) {
    return deductions.find(d => d.id === id);
}

function calculateDeductionAmount(deduction: { deductionId: string; amount: number }) {
    const deductionType = getDeductionById(deduction.deductionId);
    if (!deductionType) return 0;
    
    const baseAmount = deductionType.base === 'Subtotal' ? subTotal : subTotal;
    
    if (deductionType.mode === 'Flat') {
        return deduction.amount || 0;
    } else if (deductionType.mode === 'Rate') {
        return (baseAmount * (deduction.amount || 0)) / 100;
    }
    
    return 0;
}

</script>

<template>
    <div class="border border-gray-200 rounded-lg p-4 bg-white" v-if="model">
        <div class="flex items-center justify-between mb-3">
            <div class="flex items-center gap-2">
                <UIcon name="i-lucide-minus-circle" class="text-orange-500" />
                <span class="font-medium text-gray-700 dark:text-gray-300">
                    Déduction {{ index + 1 }}
                </span>
                <UBadge 
                    v-if="model.deductionId && getDeductionById(model.deductionId)"
                    :label="getDeductionById(model.deductionId)?.base"
                    color="info"
                    variant="soft"
                    size="xs"
                />
            </div>
            <UButton 
                icon="i-lucide-trash-2"
                color="error"
                variant="ghost"
                size="sm"
                class="rounded-full"
                @click="emit('remove')"
            />
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <UFormField 
                :label="`Type de déduction`" 
                :name="`deductions[${index}].deductionId`"
                required
            >
                <USelectMenu 
                    v-model="model.deductionId" 
                    value-key="value"
                    :items="deductions?.map(i => ({ 
                        value: i.id, 
                        label: i.title,
                        description: `${i.mode === 'Flat' ? 'Montant fixe' : 'Pourcentage'} • Base: ${i.base}`
                    }))"
                    placeholder="Sélectionner une déduction"
                >
                    <template #item="{ item }">
                        <div class="flex flex-col gap-1">
                            <span class="font-medium">{{ item.label }}</span>
                            <span class="text-xs text-gray-500">{{ item.description }}</span>
                        </div>
                    </template>
                </USelectMenu>
            </UFormField>

            <UFormField 
                :label="getDeductionById(model.deductionId)?.mode === 'Rate' ? 'Taux (%)' : 'Montant'" 
                :name="`deductions[${index}].amount`"
                required
            >
                <UInput 
                    v-model="model.amount" 
                    type="number"
                    step="0.01"
                    :placeholder="getDeductionById(model.deductionId)?.mode === 'Rate' ? '0.00' : '0.00'"
                    :icon="getDeductionById(model.deductionId)?.mode === 'Rate' ? 'i-lucide-percent' : 'i-lucide-dollar-sign'"
                />
            </UFormField>
        </div>

        <!-- Affichage du montant calculé -->
        <div 
            v-if="model.deductionId && model.amount > 0"
            class="mt-3 p-3 bg-orange-50 dark:bg-orange-900/20 rounded-lg"
        >
            <div class="flex items-center justify-between text-sm">
                <span class="text-gray-600 dark:text-gray-400">
                    Montant calculé
                    <span class="text-xs ml-1">
                        ({{ getDeductionById(model.deductionId)?.mode === 'Rate' 
                            ? `${model.amount}% de ${getDeductionById(model.deductionId)?.base}` 
                            : 'Montant fixe' }})
                    </span>
                </span>
                <span class="font-semibold text-orange-600">
                    - {{ new Intl.NumberFormat('fr-CA', { 
                        style: 'currency', 
                        currency: 'CAD' 
                    }).format(calculateDeductionAmount(model)) }}
                </span>
            </div>
        </div>
    </div>
</template>