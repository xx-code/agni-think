<script setup lang="ts">
import type { NuxtError } from '#app';
import { ModalEditProvision } from '#components';
import { deleteProvision, fetchProvision, fetchProvisions } from '~/composables/api/provisions';
import { provisionSummaryResponseToProvisionSummary } from '~/mappers/analytics';
import { provisionToProvisionCard } from '~/mappers/provision';
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

                <!-- Cards grid -->
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
            <UiProvisionCard
                v-for="provision of filteredProvisions"
                :key="provision.id"
                :data="provisionToProvisionCard(provision)"
            />
        </div>

        <!-- Empty state -->
        <UiEmptyState 
            v-if="filteredProvisions.length === 0"
            icon="i-lucide-package"
            title="Aucune provision trouvée"
            description="Ajoutez un équipement pour commencer le suivi"
            @new="openModalProvision()"
        />
    </UiPage>
</template>