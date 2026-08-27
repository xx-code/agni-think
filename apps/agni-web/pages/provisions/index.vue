<script setup lang="ts">
import { ModalEditProvision } from '#components';
import { provisionSummaryResponseToProvisionSummary } from '~/mappers/analytics';
import { listProvisionsResponseToListProvisions, provisionResponseToProvision, provisionToProvisionCard } from '~/mappers/provision';
import { ApiLinkBuilder } from '~/utils/ApiLinkBuilder';
import { API_ROUTES } from '~/shared/routes';
import type { ListResponse } from '~/types/api';
import type { GetProvisionResponse } from '~/types/api/provision';
import type { QueryFilterRequest } from '~/types/api';
import type { Provision } from '~/types/ui/provision';

const isLoadingSummary = ref(false)
const isLoading = ref(false)
const overlay = useOverlay();
const modalProvision = overlay.create(ModalEditProvision);
const toast = useToast();

const provisions = ref<Provision[]>([])
const totalProvision = ref(0)

const filter = reactive<QueryFilterRequest>({
    offset: 0,
    limit: 5,
    queryAll: false
})

const { data: provisionSummary } = useAsyncData('provision-summary', async () => {
    isLoadingSummary.value = true
    const res = ApiLinkBuilder.route(API_ROUTES.ANALYTICS.PROVISION_SUMMARY)
        .mapper(provisionSummaryResponseToProvisionSummary)
        .execute()
    isLoadingSummary.value = false

    return res
}, { watch: [ provisions ]})

async function updateProvisionList() {
    provisions.value = []
    totalProvision.value = 0
    await getAllProvisions()
}

const openModalProvision = async (provisionId?: string) => {
    let provision: Provision | undefined = undefined;
    if (provisionId) {
        provision = await ApiLinkBuilder.route<GetProvisionResponse>(API_ROUTES.PROVISIONS.GET_PROVISION).params({id: provisionId}).mapper(provisionResponseToProvision).execute();
    }
    modalProvision.open({
        provision: provision,
        onClose: (refresh) => {
            if (refresh) {
                updateProvisionList()
            }
        }
    });
}

const onDeleteProvision = async (id: string) => {
    try {
        await ApiLinkBuilder.route(API_ROUTES.PROVISIONS.DELETE_PROVISION).method('DELETE').params({id}).execute()
        updateProvisionList()
    } catch (err) {
        toast.add({ title: "Erreur suppression provision", color: 'error' });
    }
}

async function showMoreProvision() {
    if (provisions.value.length === totalProvision.value)
        return 

    filter.offset = provisions.value.length
}

async function getAllProvisions() {
    isLoading.value = true
    try {
        const res = await ApiLinkBuilder.route<ListResponse<GetProvisionResponse>>(API_ROUTES.PROVISIONS.GET_PROVISIONS)
            .query(filter)
            .mapper(listProvisionsResponseToListProvisions).execute()
        provisions.value.push(...res.items) 
        totalProvision.value = res.total
    } catch(err: any) {
        toast.add({
            title: 'Erreur Provision',
            description: err.message,
            color: 'error'
        })
    } finally {
        isLoading.value = false
    }
}


watch(filter, () => {
    getAllProvisions()
}, { immediate: true })
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
                :icon="{ name: 'i-lucide-shield-check', backgroundColor: 'rgba(168, 85, 247, 0.1)', fontColor: '#a855f7' }"
            />

            <UiBannerAccountant 
                title="Valeur initiale totale"
                :amount="provisionSummary?.initialValue ?? 0"
                :icon="{ name: 'i-lucide-vault', backgroundColor: 'rgba(59, 130, 246, 0.1)', fontColor: '#3b82f6' }"
            />

            <UiBannerAccountant 
                title="Valeur comptable actuelle"
                :amount="provisionSummary?.accountingTotalValue ?? 0"
                :icon="{ name: 'i-lucide-scale', backgroundColor: 'rgba(16, 185, 129, 0.1)', fontColor: '#10b981' }"
            />

            <UiBannerAccountant 
                title="Cout mensuelle"
                :amount="provisionSummary?.costByMonth ?? 0"
                :icon="{ name: 'i-lucide-trending-down', backgroundColor: 'rgba(239, 68, 68, 0.1)', fontColor: '#ef4444' }"
            />

            <UiBannerAccountant 
                title="Payment mensuelle"
                :amount="provisionSummary?.monthlyPayment ?? 0"
                :icon="{ name: 'i-lucide-calendar-check', backgroundColor: 'rgba(14, 165, 233, 0.1)', fontColor: '#0ea5e9' }"
            />
        </div>

        <!-- Cards grid -->
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
            <UiProvisionCard
                v-for="provision of provisions"
                :key="provision.id"
                :data="provisionToProvisionCard(provision)"
                @update="openModalProvision(provision.id)"
                @delete="onDeleteProvision(provision.id)"
            />

            <div 
                v-if="provisions.length < totalProvision"
                class="p-5 rounded-xl bg-gray-50 border border-dashed border-gray-300 h-full flex justify-center cursor-pointer hover:shadow-xs"
                @click="showMoreProvision()" >
                <div class="flex items-center my-8">
                    <span>Afficher plus</span> 
                    <UIcon name="i-lucide-arrow-right" />
                </div>                
            </div>

            <UiEmptyState 
                v-if="provisions?.length === 0 && totalProvision == 0"
                icon="i-lucide-package"
                title="Aucune provision trouvée"
                description="Ajoutez un équipement pour commencer le suivi"
                @new="openModalProvision()"
            />
        </div> 
    </UiPage>
</template>