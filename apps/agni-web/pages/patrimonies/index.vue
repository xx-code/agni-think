<script lang="ts" setup>
import type { NuxtError } from '#app';
import { ModalEditPatrimony, ModalEditSnapshotPatrimony, SlideOverPatrimonySnapshot } from '#components';
import type { TableColumn } from '#ui/types';
import { getLocalTimeZone } from '@internationalized/date';
import type { EditePatrimony, EditSnapshotPatrimony, PatrimonyType, SnapshotPatrimonyType } from '~/types/ui/patrimony';
import { fetchPatrimonies, fetchPatrimony, fetchSnapshotsPatrimony, useUpdatePatrimony, useCreatePatrimony, useUpdateSnapshotPatrimony, useAddSnapshotPatrimony, useDeletePatrimony, useRemoveSnapshotPatrimony } from '~/composables/api/patrimonies.js';
import { fetchPatrimonyEvolution, fetchPatrimonySummary } from '~/composables/api/analytics.ts';
import { patrimonyToPatrimonyCard } from '~/mappers/patrimony.ts';
import type { TypePatrimony } from '~/types/constants/patrimony.ts';

const isLoadingSummary = ref(false)
const isLoadingEvolution = ref(false)
const isLoading = ref(false)

const {data:patrimonies, refresh} = useAsyncData('patrimonies+page+all', async () => {
    isLoading.value = true
    const res = await fetchPatrimonies()
    isLoading.value = false

    return {
        assets: res.items.filter(i => i.type === 'Asset'),
        liabilities: res.items.filter(i => i.type === 'Liability')
    }
})

const { data: patrimonySummary } = useAsyncData('patrimony-summary', async () => {
    isLoadingSummary.value = true
    const res = await fetchPatrimonySummary()

    isLoadingSummary.value = false

    return res
})

const { data: patrimonyEvolutions } = useAsyncData('patrimony-evolutions', async () => {
    isLoadingEvolution.value = true
    const res = await fetchPatrimonyEvolution({
        period: 'Month',
        interval: 6
    })
    isLoadingEvolution.value = false

    return res 
})


const overlay = useOverlay()
const modalEditPatrimony = overlay.create(ModalEditPatrimony)
const slideOverSnapshot = overlay.create(SlideOverPatrimonySnapshot)


async function onSubmitPatrimony(patrimony: EditePatrimony, oldPatrimony?: PatrimonyType) {
    try {
        if (oldPatrimony)
            await useUpdatePatrimony(oldPatrimony.id, {
                title: patrimony.title,
                type: patrimony.type,
                amount: patrimony.amount,
                accountIds: patrimony.accountIds, 
            })
        else
            await useCreatePatrimony({
                title: patrimony.title,
                type: patrimony.type,
                amount: patrimony.amount,
                accountIds: patrimony.accountIds,
            })
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
        patrimony = await fetchPatrimony(id)
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
        await useDeletePatrimony(patrimonyId)
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
    <div class="space-y-6 mt-6">
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

        <!-- SECTION : Bouton ajout -->
        <div class="flex justify-end">
            <UButton 
                label="Ajouter un patrimoine" 
                icon="i-lucide-castle"
                @click="openPatrimony()" />
        </div>

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
    </div>
</template>