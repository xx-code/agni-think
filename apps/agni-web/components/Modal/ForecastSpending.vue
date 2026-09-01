<script setup lang="ts">
import { reactive, ref, shallowRef, computed } from 'vue'
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'
import type { ForcastSpending } from '~/types/ui/analytics';
import { API_ROUTES } from '~/shared/routes';
import type { ForcastSpendingRequest, ForcastSpendingResponse } from '~/types/api/analytics';
import { forcastSpendingResponseToForcastSpending } from '~/mappers/analytics';

const props = defineProps<{
    savingAccounts: { id: string, title: string }[]
}>()

const emit = defineEmits<{
    close: [close: boolean]
}>()

const step = ref<'form' | 'results'>('form')
const isLoading = ref(false)
const result = ref<ForcastSpending>()
const errorMessage = ref<string>()

function toCalendarDate(date: Date) {
    return new CalendarDate(date.getFullYear(), date.getMonth() + 1, date.getDate())
}

const today = new Date()
const inOneMonth = new Date(today.getFullYear(), today.getMonth() + 1, today.getDate())

const startDate = shallowRef(toCalendarDate(today))
const endDate = shallowRef(toCalendarDate(inOneMonth))

const wantItems = reactive<{ description: string, amount: number }[]>([])
const savingAdditionalIncome = reactive<{ savingAccountId: string, amount: number }[]>([])

const showAdvanced = ref(false)
const overrideAccountsBalance = ref<number>()
const savingRate = ref<number>()

const df = new DateFormatter('fr-CA', { dateStyle: 'medium' })

function addWantItem() {
    wantItems.push({ description: '', amount: 0 })
}
function removeWantItem(index: number) {
    wantItems.splice(index, 1)
}

function addSavingIncome() {
    savingAdditionalIncome.push({ savingAccountId: props.savingAccounts[0]?.id ?? '', amount: 0 })
}
function removeSavingIncome(index: number) {
    savingAdditionalIncome.splice(index, 1)
}

const canSubmit = computed(() =>
    wantItems.every(i => i.description.trim().length > 0 && i.amount > 0) &&
    savingAdditionalIncome.every(i => i.savingAccountId && i.amount > 0)
)

async function runForecast() {
    errorMessage.value = undefined
    isLoading.value = true
    try {
        result.value = await ApiLinkBuilder
            .route<ForcastSpendingResponse>(API_ROUTES.ANALYTICS.FORCAST_SPENDING)
            .body({
                startDate: startDate.value.toDate(getLocalTimeZone()).toISOString(),
                endDate: endDate.value.toDate(getLocalTimeZone()).toISOString(),
                wantItems: wantItems.map(i => ({ ...i })),
                savingAdditionalIncome: savingAdditionalIncome.map(i => ({ ...i })),
                overrideAccountsBalance: overrideAccountsBalance.value,
                savingRate: savingRate.value
            } as ForcastSpendingRequest)
            .mapper(forcastSpendingResponseToForcastSpending)
            .execute()
        step.value = 'results'
    } catch(err: any) {
        errorMessage.value = 'La simulation a échoué. Réessayez. Erreur: ' + (err.message  ?? "")
    } finally {
        isLoading.value = false
    }
}

function reset() {
    step.value = 'form'
    result.value = undefined
    errorMessage.value = undefined
    wantItems.splice(0, wantItems.length)
    savingAdditionalIncome.splice(0, savingAdditionalIncome.length)
    overrideAccountsBalance.value = undefined
    savingRate.value = undefined
    showAdvanced.value = false
    startDate.value = toCalendarDate(today)
    endDate.value = toCalendarDate(inOneMonth)
}

</script>

<template>
    <UModal 
        :close="{ onClick: () => $emit('close', false)}"
        title="Prévision de dépenses" 
        :ui="{ content: 'max-w-xl' }">
        <template #body>
            <!-- Step 1 : parameters simulation -->
            <div v-if="step === 'form'" class="space-y-6">
                <div class="grid grid-cols-2 gap-3">
                    <UFormField label="Du">
                        <UPopover>
                            <UButton color="neutral" variant="subtle" icon="i-lucide-calendar" class="w-full justify-start">
                                {{ df.format(startDate.toDate(getLocalTimeZone())) }}
                            </UButton>
                            <template #content>
                                <UCalendar v-model="startDate" />
                            </template>
                        </UPopover>
                    </UFormField>
                    <UFormField label="Au">
                        <UPopover>
                            <UButton color="neutral" variant="subtle" icon="i-lucide-calendar" class="w-full justify-start">
                                {{ df.format(endDate.toDate(getLocalTimeZone())) }}
                            </UButton>
                            <template #content>
                                <UCalendar v-model="endDate" />
                            </template>
                        </UPopover>
                    </UFormField>
                </div>

                <USeparator />

                <!-- Achats envisagés -->
                <div class="space-y-3">
                    <div class="flex items-center justify-between">
                        <p class="text-sm font-medium text-highlighted">Achats envisagés</p>
                        <UButton size="xs" variant="ghost" icon="i-lucide-plus" label="Ajouter" @click="addWantItem" />
                    </div>
                    <p v-if="wantItems.length === 0" class="text-sm text-neutral-400">
                        Aucun achat à simuler — la prévision portera uniquement sur vos revenus et dépenses habituels.
                    </p>
                    <div v-for="(item, idx) in wantItems" :key="idx" class="flex items-center gap-2">
                        <UInput v-model="item.description" placeholder="Description" class="flex-1" />
                        <UInput v-model="item.amount" type="number" :min="0" placeholder="Montant" class="w-28" />
                        <UButton icon="i-lucide-x" variant="ghost" color="neutral" size="sm" aria-label="Retirer" @click="removeWantItem(idx)" />
                    </div>
                </div>

                <USeparator />

                <!-- Saving added -->
                <div class="space-y-3">
                    <div class="flex items-center justify-between">
                        <p class="text-sm font-medium text-highlighted">Épargne supplémentaire</p>
                        <UButton size="xs" variant="ghost" icon="i-lucide-plus" label="Ajouter" @click="addSavingIncome" />
                    </div>
                    <p v-if="savingAdditionalIncome.length === 0" class="text-sm text-neutral-400">
                        Aucun revenu supplémentaire à épargner ajouté.
                    </p>
                    <div v-for="(item, idx) in savingAdditionalIncome" :key="idx" class="flex items-center gap-2">
                        <USelect
                            v-model="item.savingAccountId"
                            value-key="value"
                            :items="savingAccounts.map(a => ({ label: a.title, value: a.id }))"
                            class="flex-1" />
                        <UInput v-model="item.amount" type="number" :min="0" placeholder="Montant" class="w-28" />
                        <UButton icon="i-lucide-x" variant="ghost" color="neutral" size="sm" aria-label="Retirer" @click="removeSavingIncome(idx)" />
                    </div>
                </div>

                <!-- Options avancées : masquées par défaut, la plupart des simulations n'en ont pas besoin -->
                <div>
                    <button
                        type="button"
                        class="flex items-center gap-1.5 text-sm text-neutral-400 hover:text-neutral-600"
                        @click="showAdvanced = !showAdvanced"
                    >
                        <UIcon :name="showAdvanced ? 'i-lucide-chevron-up' : 'i-lucide-chevron-down'" class="text-xs" />
                        Options avancées
                    </button>
                    <div v-if="showAdvanced" class="grid grid-cols-2 gap-3 mt-3">
                        <UFormField label="Solde de comptes (remplacer)">
                            <UInput v-model="overrideAccountsBalance" type="number" placeholder="Auto" class="w-full" />
                        </UFormField>
                        <UFormField label="Taux d'épargne (%)">
                            <UInput v-model="savingRate" type="number" :min="0" :max="100" placeholder="Auto" class="w-full" />
                        </UFormField>
                    </div>
                </div>

                <p v-if="errorMessage" class="text-sm text-red-500">{{ errorMessage }}</p>

                <div class="flex justify-end gap-2 pt-2 border-t border-default">
                    <UButton label="Annuler" variant="outline" color="neutral" :disabled="isLoading" @click="emit('close', false)" />
                    <UButton
                        label="Simuler"
                        icon="i-lucide-sparkles"
                        :loading="isLoading"
                        :disabled="isLoading || !canSubmit"
                        @click="runForecast" />
                </div>
            </div>

            <!-- Step 2 : résultats -->
            <div v-else-if="result" class="space-y-6">
                <div
                    class="rounded-xl p-4 text-center"
                    :class="result.remainAmount >= 0 ? 'bg-emerald-50 dark:bg-emerald-500/10' : 'bg-red-50 dark:bg-red-500/10'"
                >
                    <p class="text-xs uppercase tracking-wide" :class="result.remainAmount >= 0 ? 'text-emerald-600' : 'text-red-600'">
                        Solde restant prévu
                    </p>
                    <p class="text-3xl font-semibold mt-1" :class="result.remainAmount >= 0 ? 'text-emerald-600' : 'text-red-600'">
                        {{ formatCurrency(result.remainAmount) }}
                    </p>
                </div>

                <div class="grid grid-cols-2 gap-3">
                    <div class="rounded-xl p-3 bg-emerald-50 dark:bg-emerald-500/10">
                        <p class="text-xs text-emerald-600">Revenus attendus</p>
                        <p class="text-lg font-semibold text-emerald-600">{{ formatCurrency(result.totalExpectedIncome) }}</p>
                    </div>
                    <div class="rounded-xl p-3 bg-red-50 dark:bg-red-500/10">
                        <p class="text-xs text-red-600">Dépenses attendues</p>
                        <p class="text-lg font-semibold text-red-600">{{ formatCurrency(result.totalExpectedExpense) }}</p>
                    </div>
                </div>

                <div class="space-y-1.5">
                    <p class="text-sm font-medium text-highlighted mb-2">Détail</p>
                    <div class="flex justify-between text-sm">
                        <span class="text-neutral-500">Revenus</span>
                        <span class="font-medium">{{ formatCurrency(result.expectedIncome) }}</span>
                    </div>
                    <div class="flex justify-between text-sm">
                        <span class="text-neutral-500">Dépenses fixes</span>
                        <span class="font-medium">{{ formatCurrency(result.expectedFixExpense) }}</span>
                    </div>
                    <div class="flex justify-between text-sm">
                        <span class="text-neutral-500">Dépenses variables</span>
                        <span class="font-medium">{{ formatCurrency(result.expectedVariableExpense) }}</span>
                    </div>
                    <div class="flex justify-between text-sm">
                        <span class="text-neutral-500">Transactions gelées prévues</span>
                        <span class="font-medium">{{ formatCurrency(result.expectedPlanFreezeExpense) }}</span>
                    </div>
                    <div class="flex justify-between text-sm">
                        <span class="text-neutral-500">Budgets</span>
                        <span class="font-medium">{{ formatCurrency(result.expectedBudgetExpense) }}</span>
                    </div>
                    <div class="flex justify-between text-sm">
                        <span class="text-neutral-500">Épargne</span>
                        <span class="font-medium">{{ formatCurrency(result.expectedSaving) }}</span>
                    </div>
                </div>

                <template v-if="result.itemsApproved.length > 0 || result.itemsRejected.length > 0">
                    <USeparator />
                    <div class="space-y-3">
                        <div v-if="result.itemsApproved.length > 0">
                            <p class="text-sm font-medium text-highlighted mb-2">Achats possibles</p>
                            <div v-for="item in result.itemsApproved" :key="item.description" class="flex items-center gap-2 text-sm py-1">
                                <UIcon name="i-lucide-check-circle-2" class="text-emerald-500 shrink-0" />
                                <span class="flex-1 truncate">{{ item.description }}</span>
                                <span class="font-medium">{{ formatCurrency(item.amount) }}</span>
                            </div>
                        </div>
                        <div v-if="result.itemsRejected.length > 0">
                            <p class="text-sm font-medium text-highlighted mb-2">Achats à reporter</p>
                            <div v-for="item in result.itemsRejected" :key="item.description" class="flex items-center gap-2 text-sm py-1">
                                <UIcon name="i-lucide-circle-x" class="text-red-500 shrink-0" />
                                <span class="flex-1 truncate">{{ item.description }}</span>
                                <span class="font-medium">{{ formatCurrency(item.amount) }}</span>
                            </div>
                        </div>
                    </div>
                </template>

                <div class="flex justify-end gap-2 pt-2 border-t border-default">
                    <UButton label="Nouvelle simulation" variant="outline" color="neutral" @click="() => {step = 'form'}" />
                    <UButton label="Fermer" @click="emit('close', false)" />
                </div>
            </div>
        </template>
    </UModal>
</template>
