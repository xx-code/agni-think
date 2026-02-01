<script setup lang="ts">
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'
import * as z from 'zod';
import { reactive, shallowRef, computed } from "vue";
import type { EditTransactionType, EditRecordType, TransactionType } from '~/types/ui/transaction';
import useAccounts from '~/composables/accounts/useAccounts';
import { useCategoriesNonSys } from '~/composables/categories/useCategories';
import useTags from '~/composables/tags/useTags';
import useBudgets from '~/composables/budgets/useBudgets';
import useTransactionTypes from '~/composables/internals/useTransactionTypes';
import type { FormSubmitEvent } from '#ui/types';
import { ALL_ACCOUNT_ID } from '~/composables/accounts/useAccountsWithPastBalance';

const { transaction, accountSelectedId } = defineProps<{
    transaction?: TransactionType
    accountSelectedId?: string
}>();

const emit = defineEmits<{
    (e: 'submit', value: EditTransactionType, oldValue?: TransactionType): void    
    (e: 'close', close: boolean): void
}>();

// Schéma de validation
const recordSchema = z.object({
    amount: z.number().gt(0, 'Le montant doit être supérieur à zéro'),
    description: z.string().nonempty('La description est requise'),
    categoryId: z.string().nonempty('Vous devez sélectionner une catégorie'),
    tagIds: z.string().array(),
    budgetIds: z.string().array()
});

const schema = z.object({
    accountId: z.string().nonempty('Vous devez sélectionner un compte'),
    status: z.enum(['Complete', 'Pending']),
    type: z.string().nonempty('Vous devez sélectionner un type'),
    records: z.array(recordSchema).min(1, 'Au moins un article est requis')
});

type Schema = z.output<typeof schema>;

// Données
const { data: accounts } = useAccounts({
    queryAll: true,
    limit: 0,
    offset: 0
});

const categories = useCategoriesNonSys({
    queryAll: true,
    limit: 0,
    offset: 0
});

const { data: tags } = useTags({
    queryAll: true,
    limit: 0,
    offset: 0
});

const { data: budgets } = useBudgets({
    queryAll: true,
    limit: 0,
    offset: 0
});

const { data: transactionTypes } = useTransactionTypes();

// État du formulaire
const form = reactive<Partial<Schema>>({
    accountId: transaction?.accountId || (accountSelectedId === ALL_ACCOUNT_ID ? '' : accountSelectedId),
    status: (transaction?.status as 'Complete' | 'Pending') || 'Pending',
    type: transaction?.type || '',
    records: transaction?.records.map(r => ({
        amount: r.amount,
        description: r.description,
        categoryId: r.categoryId,
        tagIds: r.tagRefs || [],
        budgetIds: r.budgetRefs || []
    })) || [{
        amount: 0,
        description: '',
        categoryId: '',
        tagIds: [],
        budgetIds: []
    }]
});

// Date
let valDate = transaction ? transaction.date : new Date();
const date = shallowRef(new CalendarDate(
    valDate.getFullYear(), 
    valDate.getMonth() + 1, 
    valDate.getDate()
));

const df = new DateFormatter('fr-CA', {
    dateStyle: 'medium'
});

// Calculs
const subTotal = computed(() => {
    return form.records?.reduce((sum, record) => sum + (record.amount || 0), 0) || 0;
});

// Gestion des records
function addRecord() {
    if (!form.records) form.records = [];
    form.records.push({
        amount: 0,
        description: '',
        categoryId: '',
        tagIds: [],
        budgetIds: []
    });
}

function removeRecord(index: number) {
    if (form.records && form.records.length > 1) {
        form.records.splice(index, 1);
    }
}

function duplicateRecord(index: number) {
    if (form.records) {
        const recordToDuplicate = form.records[index];
        form.records.push({
            ...recordToDuplicate,
            description: `${recordToDuplicate.description} (copie)`
        });
    }
}

// Soumission
async function onSubmit(event: FormSubmitEvent<Schema>) {
    const data = event.data;
    
    emit('submit', {
        accountId: data.accountId,
        state: data.status,
        type: data.type,
        date: date.value,
        records: data.records
    }, transaction);

    // Reset form
    form.accountId = "";
    form.status = "Pending";
    form.type = "";
    form.records = [{
        amount: 0,
        description: '',
        categoryId: '',
        tagIds: [],
        budgetIds: []
    }];

    emit('close', true);
}

// Helper pour obtenir la catégorie
function getCategoryById(id: string) {
    return categories.value.find(c => c.id === id);
}

</script>

<template>
    <UModal 
        :title="transaction ? 'Modifier la transaction' : 'Nouvelle transaction'"
        :ui="{ body: 'max-w-4xl' }"
    >
        <template #body>
            <UForm :schema="schema" :state="form" class="space-y-6" @submit="onSubmit">
                <!-- En-tête de la transaction -->
                <div class="bg-gray-50 dark:bg-gray-800 rounded-lg p-4 space-y-4">
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <UFormField label="Type de transaction" name="type" required>
                            <USelect 
                                v-model="form.type"
                                value-key="value" 
                                :items="transactionTypes?.map(i => ({ label: i.value, value: i.id }))" 
                                placeholder="Sélectionner un type"
                                size="lg"
                            />
                        </UFormField>

                        <UFormField label="Statut" name="status" required>
                            <USelect 
                                v-model="form.status"
                                :items="[
                                    { label: 'En attente', value: 'Pending' },
                                    { label: 'Validé', value: 'Complete' }
                                ]"
                                size="lg"
                            />
                        </UFormField>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <UFormField label="Compte" name="accountId" required>
                            <USelect 
                                v-model="form.accountId" 
                                value-key="value" 
                                :items="accounts?.items.map(i => ({ value: i.id, label: i.title }))" 
                                placeholder="Sélectionner un compte"
                                size="lg"
                            />
                        </UFormField>

                        <UFormField label="Date" name="date" required>
                            <UPopover>
                                <UButton 
                                    color="neutral" 
                                    variant="outline" 
                                    icon="i-lucide-calendar"
                                    size="lg"
                                    block
                                >
                                    {{ date ? df.format(date.toDate(getLocalTimeZone())) : 'Sélectionner une date' }}
                                </UButton>
                                <template #content>
                                    <UCalendar v-model="date" />
                                </template>
                            </UPopover>
                        </UFormField>
                    </div>
                </div>

                <!-- Liste des records -->
                <div class="space-y-4">
                    <div class="flex items-center justify-between">
                        <h3 class="text-lg font-semibold text-gray-900 dark:text-white">
                            Articles ({{ form.records?.length || 0 }})
                        </h3>
                        <UButton 
                            icon="i-lucide-plus" 
                            label="Ajouter un article"
                            color="primary"
                            variant="soft"
                            @click="addRecord"
                        />
                    </div>

                    <div class="space-y-4">
                        <div 
                            v-for="(record, index) in form.records" 
                            :key="index"
                            class="border border-gray-200 dark:border-gray-700 rounded-lg p-4 bg-white dark:bg-gray-900 space-y-4"
                        >
                            <!-- En-tête du record -->
                            <div class="flex items-center justify-between">
                                <div class="flex items-center gap-3">
                                    <!-- Icône de catégorie -->
                                    <div 
                                        v-if="record.categoryId"
                                        class="flex items-center justify-center rounded-full"
                                        :style="{
                                            background: `${getCategoryById(record.categoryId)?.color}22`,
                                            width: '40px',
                                            height: '40px',
                                        }"
                                    >
                                        <UIcon 
                                            :name="getCategoryById(record.categoryId)?.icon || 'i-lucide-circle'" 
                                            class="text-lg"
                                            :style="{ color: getCategoryById(record.categoryId)?.color }" 
                                        />
                                    </div>
                                    <span class="font-medium text-gray-700 dark:text-gray-300">
                                        Article {{ index + 1 }}
                                    </span>
                                </div>

                                <div class="flex items-center gap-2">
                                    <UButton 
                                        icon="i-lucide-copy"
                                        color="neutral"
                                        variant="ghost"
                                        size="sm"
                                        class="rounded-full"
                                        @click="duplicateRecord(index)"
                                    />
                                    <UButton 
                                        v-if="form.records && form.records.length > 1"
                                        icon="i-lucide-trash-2"
                                        color="error"
                                        variant="ghost"
                                        size="sm"
                                        class="rounded-full"
                                        @click="removeRecord(index)"
                                    />
                                </div>
                            </div>

                            <!-- Champs du record -->
                            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <UFormField 
                                    :label="`Montant`" 
                                    :name="`records[${index}].amount`"
                                    required
                                >
                                    <UInput 
                                        v-model="record.amount" 
                                        type="number"
                                        step="0.01"
                                        placeholder="0.00"
                                        icon="i-lucide-dollar-sign"
                                    />
                                </UFormField>

                                <UFormField 
                                    :label="`Catégorie`" 
                                    :name="`records[${index}].categoryId`"
                                    required
                                >
                                    <USelectMenu 
                                        v-model="record.categoryId" 
                                        value-key="value"
                                        :items="categories.map(i => ({ 
                                            value: i.id, 
                                            label: i.title,
                                            icon: i.icon,
                                            color: i.color
                                        }))"
                                        placeholder="Sélectionner une catégorie"
                                    >
                                        <template #item="{ item }">
                                            <div class="flex items-center gap-2">
                                                <div 
                                                    class="flex items-center justify-center rounded-full"
                                                    :style="{
                                                        background: `${item.color}22`,
                                                        width: '24px',
                                                        height: '24px',
                                                    }"
                                                >
                                                    <UIcon 
                                                        :name="item.icon" 
                                                        class="text-sm"
                                                        :style="{ color: item.color }" 
                                                    />
                                                </div>
                                                <span>{{ item.label }}</span>
                                            </div>
                                        </template>
                                    </USelectMenu>
                                </UFormField>
                            </div>

                            <UFormField 
                                :label="`Description`" 
                                :name="`records[${index}].description`"
                                required
                            >
                                <UInput 
                                    v-model="record.description"
                                    placeholder="Ex: Épicerie du mois"
                                />
                            </UFormField>

                            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <UFormField :label="`Tags`" :name="`records[${index}].tagIds`">
                                    <UInputMenu 
                                        v-model="record.tagIds" 
                                        multiple 
                                        value-key="value"
                                        :items="tags?.items.map(i => ({ 
                                            value: i.id, 
                                            label: i.value,
                                            color: i.color 
                                        }))"
                                        placeholder="Sélectionner des tags"
                                    >
                                        <template #item="{ item }">
                                            <UBadge 
                                                :label="item.label"
                                                :style="{ borderColor: item.color, color: item.color }"
                                                variant="outline"
                                                size="sm"
                                            />
                                        </template>
                                    </UInputMenu>
                                </UFormField>

                                <UFormField :label="`Budgets`" :name="`records[${index}].budgetIds`">
                                    <UInputMenu 
                                        v-model="record.budgetIds" 
                                        multiple 
                                        value-key="value" 
                                        :items="budgets?.items.map(i => ({ value: i.id, label: i.title }))"
                                        placeholder="Sélectionner des budgets"
                                    >
                                        <template #item="{ item }">
                                            <div class="flex items-center gap-2">
                                                <UIcon name="i-lucide-pie-chart" class="text-primary-500" />
                                                <span>{{ item.label }}</span>
                                            </div>
                                        </template>
                                    </UInputMenu>
                                </UFormField>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Récapitulatif -->
                <div class="bg-gray-50 dark:bg-gray-800 rounded-lg p-4">
                    <div class="flex justify-between items-center">
                        <span class="text-lg font-semibold text-gray-900 dark:text-white">
                            Total
                        </span>
                        <span 
                            class="text-2xl font-bold"
                            :class="form.type !== 'Income' ? 'text-red-600' : 'text-green-600'"
                        >
                            {{ new Intl.NumberFormat('fr-CA', { 
                                style: 'currency', 
                                currency: 'CAD' 
                            }).format(subTotal) }}
                        </span>
                    </div>
                    <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
                        {{ form.records?.length || 0 }} article{{ (form.records?.length || 0) > 1 ? 's' : '' }}
                    </p>
                </div>

                <!-- Actions -->
                <div class="flex justify-end gap-3 pt-4 border-t border-gray-200 dark:border-gray-700">
                    <UButton 
                        label="Annuler" 
                        color="neutral"
                        variant="outline"
                        size="lg"
                        @click="emit('close', false)" 
                    />
                    <UButton 
                        type="submit"
                        :label="transaction ? 'Mettre à jour' : 'Créer'"
                        icon="i-lucide-check"
                        size="lg"
                    />
                </div>
            </UForm>
        </template>
    </UModal> 
</template>

<style scoped>
/* Animation pour les nouveaux records */
@keyframes slideIn {
    from {
        opacity: 0;
        transform: translateY(-10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.space-y-4 > div {
    animation: slideIn 0.2s ease-out;
}
</style>