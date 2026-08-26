<script setup lang="ts">
import type { NuxtError } from '#app';
import { ModalEditProvision } from '#components';
import { getLocalTimeZone } from '@internationalized/date';
import { createProvision, deleteProvision, fetchProvision, fetchProvisions, updateProvision } from '~/composables/api/provisions';
import { provisionSummaryResponseToProvisionSummary } from '~/mappers/analytics';
import { API_ROUTES } from '~/shared/routes';
import type { EditProvisionType, Provision } from '~/types/ui/provision';

const isLoadingSummary = ref(false)
const overlay = useOverlay();
const modalProvision = overlay.create(ModalEditProvision);
const toast = useToast();

const { data: provisionSummary } = useAsyncData('provision-summary', async () => {
    isLoadingSummary.value = true
    const res = ApiLinkBuilder.route(API_ROUTES.ANALYTICS.PROVISION_SUMMARY)
        .mapper(provisionSummaryResponseToProvisionSummary)
        .execute()
    isLoadingSummary.value = false

    return res
}, { watch: []})

const { data, error, refresh } = useAsyncData('provision+all', async () => {
    const res = await fetchProvisions({ queryAll: true, offset: 0, limit: 0 })
    return res
})

async function onSubmitProvision(value: EditProvisionType, oldValue?: Provision) {
    try {
        // if (oldValue) {
        //     await updateProvision(oldValue.id, {
        //         title: value.title,
        //         initialCost: value.initialCost,
        //         acquisitionDate: value.acquisitionDate?.toDate(getLocalTimeZone()).toISOString(),
        //         expectedLifespanMonth: value.expectedLifespanMonth,
        //         residualValue: value.residualValue
        //     })
        // } else {
        //     await createProvision({
        //         title: value.title,
        //         initialCost: value.initialCost,
        //         acquisitionDate: value.acquisitionDate.toDate(getLocalTimeZone()).toISOString(),
        //         expectedLifespanMonth: value.expectedLifespanMonth,
        //         residualValue: value.residualValue
        //     })
        // }
        await refresh()
    } catch (err) {
        const nuxtError = err as NuxtError
        toast.add({ title: "Erreur provision", color: 'error' });
    }
}

const openModalProvision = async (provisionId?: string) => {
    let provision: Provision | undefined = undefined;
    if (provisionId) {
        provision = await fetchProvision(provisionId);
    }
    modalProvision.open({
        provision: provision,
        onSubmit: onSubmitProvision
    });
}

const onDeleteProvision = async (id: string) => {
    try {
        await deleteProvision(id)
        refresh()
    } catch (err) {
        toast.add({ title: "Erreur suppression provision", color: 'error' });
    }
}

// --- Amortization helpers ---

function getElapsedMonths(acquisitionDate: Date): number {
    const now = new Date()
    const acq = new Date(acquisitionDate)
    return Math.max(0, (now.getFullYear() - acq.getFullYear()) * 12 + (now.getMonth() - acq.getMonth()))
}

function getAmortizationPercent(provision: Provision): number {
    const elapsed = getElapsedMonths(provision.acquisitionDate)
    return Math.min(100, Math.round((elapsed / provision.expectedLifespanMonth) * 100))
}


function getMonthlyProvision(provision: Provision): number {
    const depreciable = provision.initialCost - provision.residualValue
    return depreciable / provision.expectedLifespanMonth
}

function getRemainingMonths(provision: Provision): number {
    const elapsed = getElapsedMonths(provision.acquisitionDate)
    return Math.max(0, provision.expectedLifespanMonth - elapsed)
}

function getStatusColor(percent: number): string {
    if (percent >= 100) return 'text-gray-400'
    if (percent >= 75) return 'text-red-500'
    if (percent >= 40) return 'text-yellow-500'
    return 'text-emerald-500'
}

function getBarColor(percent: number): string {
    if (percent >= 100) return 'bg-gray-300'
    if (percent >= 75) return 'bg-red-400'
    if (percent >= 40) return 'bg-yellow-400'
    return 'bg-emerald-400'
}

function formatCurrency(value: number): string {
    return new Intl.NumberFormat('fr-CA', {
        style: 'currency',
        currency: 'CAD',
        maximumFractionDigits: 0
    }).format(value)
}

function formatDate(date: Date): string {
    return new Date(date).toLocaleDateString('fr-CA', { year: 'numeric', month: 'short' })
}

// --- Summary ---

const provisions = computed(() => data.value?.items ?? [])

const searchQuery = ref('')

const filteredProvisions = computed(() => {
    if (!searchQuery.value) return provisions.value
    return provisions.value.filter(p =>
        p.title.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
})
</script>

<template>
    <UiPage>
        <UiPageHeader 
            title="Provisions"
            subtitle="Suivi de l'amortissement de vos équipements"
            :button="{
                icon: 'i-lucide-plus',
                label: 'Nouvelle provision'
            }"
            @click-button="openModalProvision()"
        /> 

        <!-- Summary cards -->
        <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
            <UiBannerAccountant 
                title="Provisions actives"
                :amount="provisionSummary?.activesProvision ?? 0"
                :icon="{ name: 'i-lucide-target', backgroundColor: 'rgba(168, 85, 247, 0.1)', fontColor: '#a855f7'}"
            />

            <UiBannerAccountant 
                title="Valeur initiale totale"
                :amount="provisionSummary?.initialValue ?? 0"
                :icon="{ name: 'i-lucide-target', backgroundColor: 'rgba(23, 85, 247, 0.1)', fontColor: '#a855f7'}"
            />

            <UiBannerAccountant 
                title="Valeur comptable actuelle"
                :amount="provisionSummary?.accountingTotalValue ?? 0"
                :icon="{ name: 'i-lucide-target', backgroundColor: 'rgba(102, 100, 43, 0.1)', fontColor: '#a855f7'}"
            />

            <UiBannerAccountant 
                title="Cout mensuelle"
                :amount="provisionSummary?.costByMonth ?? 0"
                :icon="{ name: 'i-lucide-target', backgroundColor: 'rgba(235, 23, 54, 0.1)', fontColor: '#a855f7'}"
            />

            <UiBannerAccountant 
                title="Payment mensuelle"
                :amount="provisionSummary?.monthlyPayment ?? 0"
                :icon="{ name: 'i-lucide-target', backgroundColor: 'rgba(168, 245, 247, 0.1)', fontColor: '#a855f7'}"
            />
            
        </div>

        <!-- Provisions list -->
        <UCard>
            <div class="space-y-6">

                <!-- Search + count -->
                <div class="flex items-center justify-between gap-4">
                    <div class="max-w-sm w-full">
                        <UInput
                            v-model="searchQuery"
                            icon="i-lucide-search"
                            placeholder="Rechercher une provision..."
                            size="md"
                        />
                    </div>
                    <p class="text-sm text-gray-400 shrink-0">
                        {{ filteredProvisions.length }} élément{{ filteredProvisions.length > 1 ? 's' : '' }}
                    </p>
                </div>

                <!-- Cards grid -->
                <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
                    <div
                        v-for="provision of filteredProvisions"
                        :key="provision.id"
                        class="group relative flex flex-col gap-4 p-5 bg-white border border-gray-100 rounded-2xl shadow-sm hover:shadow-md transition-all duration-200 hover:-translate-y-0.5"
                    >
                        <!-- Card header -->
                        <div class="flex items-start justify-between gap-2">
                            <div class="flex items-center gap-3 min-w-0">
                                <div class="flex items-center justify-center w-10 h-10 shrink-0 rounded-xl bg-gray-50">
                                    <UIcon name="i-lucide-box" class="w-5 h-5 text-gray-600" />
                                </div>
                                <div class="min-w-0">
                                    <h3 class="font-bold text-gray-900 leading-tight truncate">{{ provision.title }}</h3>
                                    <p class="text-xs text-gray-400">Acquis {{ formatDate(provision.acquisitionDate) }}</p>
                                </div>
                            </div>

                            <!-- Status badge -->
                            <span
                                class="shrink-0 inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold"
                                :class="{
                                    'bg-gray-100 text-gray-500': getAmortizationPercent(provision) >= 100,
                                    'bg-red-50 text-red-600': getAmortizationPercent(provision) >= 75 && getAmortizationPercent(provision) < 100,
                                    'bg-yellow-50 text-yellow-600': getAmortizationPercent(provision) >= 40 && getAmortizationPercent(provision) < 75,
                                    'bg-emerald-50 text-emerald-600': getAmortizationPercent(provision) < 40,
                                }"
                            >
                                <span v-if="getAmortizationPercent(provision) >= 100">Amorti</span>
                                <span v-else>{{ getAmortizationPercent(provision) }}%</span>
                            </span>
                        </div>

                        <!-- Amortization progress bar -->
                        <div class="space-y-1.5">
                            <div class="flex justify-between items-center">
                                <span class="text-xs text-gray-500">Amortissement</span>
                                <span class="text-xs font-medium text-gray-500">
                                    <template v-if="getRemainingMonths(provision) > 0">
                                        {{ getRemainingMonths(provision) }} mois restants
                                    </template>
                                    <template v-else>
                                        Durée de vie atteinte
                                    </template>
                                </span>
                            </div>
                            <div class="h-2 w-full bg-gray-100 rounded-full overflow-hidden">
                                <div
                                    class="h-full rounded-full transition-all duration-500"
                                    :class="getBarColor(getAmortizationPercent(provision))"
                                    :style="{ width: getAmortizationPercent(provision) + '%' }"
                                />
                            </div>
                        </div>

                        <!-- Key metrics -->
                        <div class="grid grid-cols-3 gap-2 pt-2 border-t border-gray-50">
                            <div class="text-center">
                                <p class="text-xs text-gray-400 mb-0.5">Valeur initiale</p>
                                <p class="text-sm font-semibold text-gray-700">{{ formatCurrency(provision.initialCost) }}</p>
                            </div>
                            <div class="text-center border-x border-gray-100">
                                <p class="text-xs text-gray-400 mb-0.5">Valeur actuelle</p>
                                <p class="text-sm font-bold" :class="getStatusColor(getAmortizationPercent(provision))">
                                    {{ formatCurrency(provision.residualValue) }}
                                </p>
                            </div>
                            <div class="text-center">
                                <p class="text-xs text-gray-400 mb-0.5">Mensualité</p>
                                <p class="text-sm font-semibold text-gray-700">{{ formatCurrency(getMonthlyProvision(provision)) }}</p>
                            </div>
                        </div>

                        <!-- Footer -->
                        <div class="flex items-center justify-between">
                            <div class="flex items-center gap-1.5 text-xs text-gray-400">
                                <UIcon name="i-lucide-clock" class="w-3.5 h-3.5" />
                                <span>{{ provision.expectedLifespanMonth }} mois · Résiduel {{ formatCurrency(provision.residualValue) }}</span>
                            </div>
                            <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                                <UButton
                                    variant="ghost"
                                    color="neutral"
                                    icon="i-lucide-pencil"
                                    size="xs"
                                    @click="openModalProvision(provision.id)"
                                />
                                <UButton
                                    variant="ghost"
                                    color="error"
                                    icon="i-lucide-trash-2"
                                    size="xs"
                                    @click="onDeleteProvision(provision.id)"
                                />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Empty state -->
                <div
                    v-if="filteredProvisions.length === 0"
                    class="text-center py-16 bg-gray-50 rounded-xl border-2 border-dashed border-gray-200"
                >
                    <UIcon name="i-lucide-package" class="w-12 h-12 text-gray-300 mx-auto mb-3" />
                    <p class="text-gray-500 font-medium text-sm">Aucune provision trouvée</p>
                    <p class="text-gray-400 text-xs mt-1 mb-4">Ajoutez un équipement pour commencer le suivi</p>
                    <UButton
                        label="Créer une provision"
                        size="sm"
                        @click="openModalProvision()"
                    />
                </div>

            </div>
        </UCard>
    </UiPage>
</template>