<script lang="ts" setup>
import type { NuxtError } from '#app';
import { ModalEditPatrimony, SlideOverPatrimonySnapshot } from '#components';
import type { EditePatrimony , PatrimonyType  } from '~/types/ui/patrimony';
import { ApiLinkBuilder } from '~/utils/ApiLinkBuilder';
import { API_ROUTES } from '~/shared/routes';
import { patrimonyResponseToPatrimony, patrimonyToPatrimonyCard } from '~/mappers/patrimony';
import { patrimonyEvolutionResponseToPatrimonyEvolution } from '~/mappers/analytics';
import type { CreatedRequest, ListResponse, QueryFilterRequest } from '~/types/api';
import type { GetPatrimonyResponse } from '~/types/api/patrimony';
import type { QueryPatrimonyEvolution, PatrimonySummaryResponse } from '~/types/api/analytics';

const isLoadingSummary = ref(false)
const isLoadingEvolution = ref(false)
const isLoading = ref(false)

const {data:patrimonies, refresh} = useAsyncData('patrimonies+page+all', async () => {
    isLoading.value = true
    const query: QueryFilterRequest = { limit: 0, offset: 0, queryAll: true }
    const res = await ApiLinkBuilder.route<ListResponse<GetPatrimonyResponse>>(API_ROUTES.PATRIMONIES.GET_PATRIMONIES).query(query).execute()
    const mapped = {
        items: res.items.map(i => patrimonyResponseToPatrimony(i)),
        total: Number(res.total)
    }
    isLoading.value = false

    return {
        assets: mapped.items.filter(i => i.type === 'Asset'),
        liabilities: mapped.items.filter(i => i.type === 'Liability')
    }
})

const { data: patrimonySummary } = useAsyncData('patrimony-summary', async () => {
    isLoadingSummary.value = true
    const res = await ApiLinkBuilder.route<PatrimonySummaryResponse>(API_ROUTES.ANALYTICS.PATRIMONY_SUMMARY).execute()

    isLoadingSummary.value = false

    return res
})

const { data: patrimonyEvolutions } = useAsyncData('patrimony-evolutions', async () => {
    isLoadingEvolution.value = true
    const res = await ApiLinkBuilder.route(API_ROUTES.ANALYTICS.PATRIMONY_EVOLUTION).query({
        period: 'Month',
        interval: 6
    } as QueryPatrimonyEvolution).mapper(patrimonyEvolutionResponseToPatrimonyEvolution).execute()
    isLoadingEvolution.value = false

    return res 
})


const overlay = useOverlay()
const modalEditPatrimony = overlay.create(ModalEditPatrimony)
const slideOverSnapshot = overlay.create(SlideOverPatrimonySnapshot)


async function onSubmitPatrimony(patrimony: EditePatrimony, oldPatrimony?: PatrimonyType) {
    try {
        if (oldPatrimony)
            await ApiLinkBuilder.route(API_ROUTES.PATRIMONIES.UPDATE_PATRIMONY).params({id: oldPatrimony.id}).body({
                title: patrimony.title,
                type: patrimony.type,
                amount: patrimony.amount,
                accountIds: patrimony.accountIds, 
            }).execute()
        else
            await ApiLinkBuilder.route<CreatedRequest>(API_ROUTES.PATRIMONIES.CREATE_PATRIMONY).body({
                title: patrimony.title,
                type: patrimony.type,
                amount: patrimony.amount,
                accountIds: patrimony.accountIds,
            }).execute()
        refresh()
    } catch(err) {
        console.log(err)
        alert("error create")
    }
}

async function openPatrimony(id?: string) {
    if (id === 'SAVE_GOAL')
        return

    let patrimony: PatrimonyType|undefined 
    if (id)
        patrimony = await ApiLinkBuilder.route<GetPatrimonyResponse>(API_ROUTES.PATRIMONIES.GET_PATRIMONY).params({id}).mapper(patrimonyResponseToPatrimony).execute()
    modalEditPatrimony.open({
        patrimony: patrimony,
        onSubmit: onSubmitPatrimony
    })
}



async function onClickPatrimonyCard(id: string, isFund: boolean) {
    const instance = slideOverSnapshot.open({
        id: id,
        isFund: isFund,
        onClose: (doRefresh) => {
            if (doRefresh) {
                refresh()
            }
        }
    })
    await instance.result
}

async function deletePatrimony(patrimonyId: string) {
    try {
    const isOk = confirm("Voulez vous supprmier le patrimoine")
    if (isOk) {
        await ApiLinkBuilder.route(API_ROUTES.PATRIMONIES.DELETE_PATRIMONY).params({id: patrimonyId}).execute()
        refresh()
    }
    } catch(err) {
        console.log(err)
        const nuxtError = err as NuxtError
        alert("Error: " + nuxtError?.message || 'Error Patrimony')
    } 
}

</script>

<template> 
    <UiPage>
        <UiPageHeader 
            title="Mon patrimoine"
            :button="
                {
                    icon: 'i-lucide-plus',
                    label: 'Ajouter un actif/passif'
                }
            "
            subtitle="Voir et gerer l'evolution de mon patrimoine"
            @click-button="openPatrimony()"
        />

        <UiPatrimonyHeader 
            :networth="patrimonySummary?.networth ?? 0"
            :monthly-evolution="patrimonySummary?.monthlyEvolutionPerc ?? 0"
            :total-asset="patrimonySummary?.totalAsset ?? 0"
            :total-liability="patrimonySummary?.totalLiability ?? 0"
        />

        <UiPatrimonyGraph 
            :net-worth-dates="patrimonyEvolutions?.networthByPeriod.map(i => i.date) ?? []"
            :net-worth-evolutions="patrimonyEvolutions?.networthByPeriod.map(i => i.networth) ?? []"
            :asset-labels="patrimonies?.assets.map(i => i.title) ?? []"
            :asset-amounts="patrimonies?.assets.map(i => i.currentBalance) ?? []"
            :liability-labels="patrimonies?.liabilities.map(i => i.title) ?? []"
            :liability-amounts="patrimonies?.liabilities.map(i => i.amount) ?? []"
        />

        <div>
            <div class="mb-5">
                <span class="text-lg font-bold">Actifs</span>
                <span class="ml-1">{{ formatCurrency(patrimonySummary?.totalAsset ?? 0) }}</span>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-5">
                <UiPatrimonyCard 
                    v-for="asset in patrimonies?.assets"
                    :key="asset.id"
                    :patrimony="patrimonyToPatrimonyCard(asset)"
                    @click="onClickPatrimonyCard(asset.id, asset.totalFund)"
                    @update="openPatrimony(asset.id)"
                    @delete="deletePatrimony(asset.id)"
                />
            </div>
        </div> 

        <div>
            <div class="mb-5">
                <span class="text-lg font-bold">Passifs</span>
                <span class="ml-1">{{ formatCurrency(patrimonySummary?.totalLiability ?? 0) }}</span>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-3 gap-5">
                <UiPatrimonyCard 
                    v-for="liability in patrimonies?.liabilities"
                    :key="liability.id"
                    :patrimony="patrimonyToPatrimonyCard(liability)"
                    @click="onClickPatrimonyCard(liability.id, liability.totalFund)"
                    @update="openPatrimony(liability.id)"
                    @delete="deletePatrimony(liability.id)"
                />
            </div>
        </div>
    </UiPage>
</template>