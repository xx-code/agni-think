<script lang="ts" setup>
import type { NuxtError } from '#app';
import { ModalEditPatrimony, ModalEditSnapshotPatrimony } from '#components';
import type { TableColumn } from '#ui/types';
import { getLocalTimeZone } from '@internationalized/date';
import { useAddSnapshotPatrimony } from '~/composables/patrimonies/useAddSnapshotPatrimony';
import { useCreatePatrimony } from '~/composables/patrimonies/useCreatePatrimony';
import { useDeletePatrimony } from '~/composables/patrimonies/useDeletePatrimony';
import { usePatrimonies } from '~/composables/patrimonies/usePatrimonies';
import { fetchPatrimony } from '~/composables/patrimonies/usePatrimony';
import { useRemoveSnapshotPatrimony } from '~/composables/patrimonies/useRemoveSnapshotPatrimony';
import { fetchSnapshotsPatrimony } from '~/composables/patrimonies/useSnapshotsPatrimony';
import { useUpdatePatrimony } from '~/composables/patrimonies/useUpdatePatrimony';
import { useUpdateSnapshotPatrimony } from '~/composables/patrimonies/useUpdateSnapshotPatrimony';
import type { EditePatrimony, EditSnapshotPatrimony, PatrimonyType, SnapshotPatrimonyType, TypePatrimony } from '~/types/ui/patrimony';
import AssetCard from './AssetCard.vue';

const {data:patrimonies, refresh} = usePatrimonies()


const overlay = useOverlay()
const modalEditPatrimony = overlay.create(ModalEditPatrimony)
const modalEditSnapshotPatrimony = overlay.create(ModalEditSnapshotPatrimony)

const snapshots = ref<SnapshotPatrimonyType[]>([])

const totalPatrimony = computed(() => {
    const assets = patrimonies.value?.items.filter(i => i.type.toLowerCase() === 'asset')
    const liablities = patrimonies.value?.items.filter(i => i.type.toLowerCase() === 'liability')

    const currentAmountAsset = assets?.reduce((acc: number, asset) => acc + asset.currentBalance, 0)
    const lastAmountAsset = assets?.reduce((acc: number, asset) => acc + asset.lastSnapshotBalance, 0)
    const currentAmountLiability = liablities?.reduce((acc: number, asset) => acc + asset.currentBalance, 0)
    const lastAmountLiability = liablities?.reduce((acc: number, asset) => acc + asset.lastSnapshotBalance, 0)

    return {

        currentAmountAsset: currentAmountAsset || 0,
        lastAmountAsset: lastAmountAsset || 0,
        currentAmountLiability: currentAmountLiability || 0,
        lastAmountLiability: lastAmountLiability || 0,
        currentBalance: (currentAmountAsset || 0) - (currentAmountLiability || 0), 
        lastBalance: (lastAmountAsset || 0) - (lastAmountLiability || 0),
        totalAmount: (currentAmountAsset || 0) + (currentAmountLiability || 0)
    } 
})

const selectedPatrimony = ref<PatrimonyType>()

const optionsChart = computed(() => ({responsive: true, plugins: {colors: { forceOverride: true}}})) 
const patriomiesDonutChart = computed(() => {
    let labels: string[] = [] 
    const data: number[]= []
    const reactiveColor: string[] = []
    if (patrimonies.value){
        labels = patrimonies.value.items.map(pat => pat.title)
        patrimonies.value.items.forEach(pat => {
            let value = pat.currentBalance 
            if (pat.type !== 'Asset' && value > 0)
              value *= -1
            data.push(value)
        })
    }

    return {
        labels: labels,
        datasets: [{
            label: 'Somme',
            data: data,
        }]
    } 
});

async function onClickPatrimony(id: string) {
    try {
        const res = await fetchPatrimony(id)
        const resSnapshots = await fetchSnapshotsPatrimony(id, new Date('2025-08-20'), new Date('2025-08-26'))
        selectedPatrimony.value = res
        snapshots.value = resSnapshots.items
    } catch(err) {
        console.log(err)
        alert("Patrimony error")
    }
}

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

async function onSubmitSnapshot(snapshot: EditSnapshotPatrimony, oldSnapshot?: SnapshotPatrimonyType) {
    try {
        if (oldSnapshot)
            await useUpdateSnapshotPatrimony(oldSnapshot.patrimonyId, oldSnapshot.id, {
                status: snapshot.status,
                balance: snapshot.balance,
                date: snapshot.date.toDate(getLocalTimeZone()).toISOString()
            })
        else
            await useAddSnapshotPatrimony(selectedPatrimony.value?.id || '', {
                balance: snapshot.balance,
                status: snapshot.status,
                date: snapshot.date.toDate(getLocalTimeZone()).toISOString()
            })
        await onClickPatrimony(selectedPatrimony.value?.id || '')
        refresh()
    } catch(err) {
        console.log(err)
        const nuxtError = err as NuxtError
        alert("Error: " + nuxtError?.cause || 'Error Patrimony')
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

function openSnapshot(snapshot?: SnapshotPatrimonyType) {
    modalEditSnapshotPatrimony.open({
        snapshot: snapshot,
        onSubmit: onSubmitSnapshot
    })
}

async function deletePatrimony(patrimonyId: string) {
    try {
    const isOk = confirm("Voulez vous supprmier le patrimoine")
    if (isOk) {
        await useDeletePatrimony(patrimonyId)
        selectedPatrimony.value = undefined
        snapshots.value = []
        refresh()
    }
    } catch(err) {
        console.log(err)
        const nuxtError = err as NuxtError
        alert("Error: " + nuxtError?.message || 'Error Patrimony')
    } 
}

async function removeSnapshot(patrimonyId: string, snapshotId: string) {
    await useRemoveSnapshotPatrimony(patrimonyId, snapshotId)
    refresh()
    onClickPatrimony(patrimonyId)
}

const computeDiff = (currentBalance: number, accountBalance: number, type: TypePatrimony) => {
    if (type === 'Asset')
        return currentBalance - accountBalance 
    return  accountBalance - currentBalance 
}

const UButton = resolveComponent('UButton')
const columns: TableColumn<SnapshotPatrimonyType>[] = [
    {
        accessorKey: 'balance',
        header: 'Balance',
        cell: ({ row }) =>  formatCurrency(row.original.balance) 
    },
    {
        accessorKey: 'status',
        header: 'Status',
        cell: ({ row }) =>  row.original.status
    },
    {
        accessorKey: 'date',
        header: 'Date',
        cell: ({ row }) => formatDate(row.original.date)
    },
    {
        id: 'action',
        header: '',
        cell: ({ row }) => {
            return (
                h('div', { class: 'space-x-1 text-right'},
                    [
                        h(UButton, { variant: 'ghost', color: 'info', icon: 'i-lucide-edit', onClick: () => openSnapshot(row.original) }),
                        h(UButton, { variant: 'ghost', color: 'error', icon: 'i-lucide-trash', onClick: () => removeSnapshot(row.original.patrimonyId, row.original.id) }),
                    ]
                )
            )
        }
    }
]

function computerPercentagePatrimony(totalAmount: number, assetAmount: number) {
    return roundNumber(((assetAmount * 100) / totalAmount))
}

</script>

<template> 
  <div class="space-y-6 mt-6">
    <!-- SECTION : Résumé global -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <UCard class="rounded-lg shadow-sm">
        <h4 class="text-sm font-medium text-gray-400">Valeur nette totale</h4>
        <p class="font-bold text-2xl mt-2 text-gray-800">
          {{ formatCurrency(totalPatrimony.currentBalance) }}
        </p>
      </UCard>

      <UCard class="rounded-lg shadow-sm">
        <h4 class="text-sm font-medium text-gray-400">Évolution mensuelle</h4>
        <p 
          :class="[
            'font-bold text-2xl mt-2 flex items-center gap-1',
            computeDiff(totalPatrimony.currentBalance, totalPatrimony.lastBalance, 'Asset') > 0 
              ? 'text-green-500' 
              : 'text-red-500'
          ]">
          <span>
            {{ computeDiff(totalPatrimony.currentBalance, totalPatrimony.lastBalance, 'Asset') > 0 ? '+' : '-' }}
          </span>
          {{ Math.abs(roundNumber(computePercentage(totalPatrimony.currentBalance, totalPatrimony.lastBalance))) }}%
        </p>
      </UCard>

      <UCard class="rounded-lg shadow-sm">
        <h4 class="text-sm font-medium text-gray-400">Actifs vs Passifs</h4>
        <p class="font-bold text-2xl mt-2 text-gray-700">
          <span class="text-green-600">
            {{ computerPercentagePatrimony(totalPatrimony.totalAmount, totalPatrimony.currentAmountAsset) }}%
          </span> / 
          <span class="text-red-500">
            {{ computerPercentagePatrimony(totalPatrimony.totalAmount, totalPatrimony.currentAmountLiability) }}%
          </span>
        </p>
      </UCard>
    </div>

    <!-- SECTION : Graphiques -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <UCard class="rounded-lg shadow-sm">
        <CustomCardTitle title="Répartition du patrimoine"/>
        <div class="flex justify-center items-center h-[280px]">
          <DoughnutChart :data="patriomiesDonutChart" :options="optionsChart"/>
        </div>
      </UCard>

      <UCard class="rounded-lg shadow-sm">
        <CustomCardTitle title="Répartition des actifs"/>
        <div class="flex justify-center items-center h-[280px] text-gray-400 italic">
          (À venir)
        </div>
      </UCard>
    </div>

    <!-- SECTION : Bouton ajout -->
    <div class="flex justify-end">
      <UButton 
        label="Ajouter un patrimoine" 
        icon="i-lucide-castle"
        @click="openPatrimony()" />
    </div>

    <!-- SECTION : Liste patrimoines -->

    <div class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-4">
      <div 
        v-for="patrimony in patrimonies?.items.filter(i => i.type.toLowerCase() == 'asset')" 
        :key="patrimony.id">
        <AssetCard 
            :patrimony="patrimony"
            @open="onClickPatrimony(patrimony.id)"
            @delete="deletePatrimony(patrimony.id)"
            @update="openPatrimony(patrimony.id)"
        /> 
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-4">
      <div 
        v-for="patrimony in patrimonies?.items.filter(i => i.type.toLocaleLowerCase() == 'liability')" 
            :key="patrimony.id">
            <AssetCard 
                :patrimony="patrimony"
                @open="onClickPatrimony(patrimony.id)"
                @delete="deletePatrimony(patrimony.id)"
                @update="openPatrimony(patrimony.id)"
            />    
      </div>
    </div>

    <!-- SECTION : Table snapshots -->
    <UCard class="mt-4 rounded-lg shadow-sm">
        <div class="flex justify-between items-center">
            <CustomCardTitle :title="'Historique des snapshots - ' + (selectedPatrimony?.title ?? '')"/>
            <UButton label="Snapshot" icon="i-lucide-circle-fading-plus"  @click="openSnapshot()"/>
        </div>
        <UTable 
            :columns="columns" 
            :data="snapshots"
            class="mt-2"
        />
    </UCard>
  </div>
</template>