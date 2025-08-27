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
import type { EditePatrimony, EditSnapshotPatrimony, PatrimonyDetailType, SnapshotPatrimonyType, TypePatrimony } from '~/types/ui/patrimony';

const {data:patrimonies, refresh} = usePatrimonies()


const overlay = useOverlay()
const modalEditPatrimony = overlay.create(ModalEditPatrimony)
const modalEditSnapshotPatrimony = overlay.create(ModalEditSnapshotPatrimony)

const snapshots = ref<SnapshotPatrimonyType[]>([])

const totalPatrimony = computed(() => {
    const assets = patrimonies.value?.items.filter(i => i.type === 'Asset')
    const liablities = patrimonies.value?.items.filter(i => i.type === 'Liability')

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

const selectedPatrimony = ref<PatrimonyDetailType>()

const optionsChart = computed(() => ({responsive: true, plugins: {colors: { forceOverride: true}}})) 
const patriomiesDonutChart = computed(() => {
    let labels: string[] = [] 
    const data: number[]= []
    const reactiveColor: string[] = []
    if (patrimonies.value){
        labels = patrimonies.value.items.map(pat => pat.title)
        patrimonies.value.items.forEach(pat => {
            data.push(pat.type === 'Asset' ? pat.currentBalance :  -1*pat.currentBalance)
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

async function onSubmitPatrimony(patrimony: EditePatrimony, oldPatrimony?: PatrimonyDetailType) {
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
                patrimonyId: selectedPatrimony.value?.id || '',
                balance: snapshot.balance,
                status: snapshot.status,
                date: snapshot.date.toDate(getLocalTimeZone()).toISOString()
            })
        await onClickPatrimony(selectedPatrimony.value?.id || '')
        refresh()
    } catch(err) {
        const nuxtError = err as NuxtError
        alert("Error: " + nuxtError?.cause || 'Error Patrimony')
    }
}

async function openPatrimony(id?: string) {
    if (id === 'SAVE_GOAL')
        return

    let patrimony: PatrimonyDetailType|undefined 
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
    await useDeletePatrimony(patrimonyId)
    selectedPatrimony.value = undefined
    snapshots.value = []
    refresh()
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
                h('div', { class: 'space-x-1'},
                    [
                        h(UButton, { icon: 'i-lucide-edit', onClick: () => openSnapshot(row.original) }),
                        h(UButton, { icon: 'i-lucide-trash', onClick: () => removeSnapshot(row.original.patrimonyId, row.original.id) }),
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
      <UCard class="rounded-2xl shadow-sm">
        <h4 class="text-sm font-medium text-gray-400">Valeur nette totale</h4>
        <p class="font-bold text-2xl mt-2 text-gray-800">
          {{ formatCurrency(totalPatrimony.currentBalance) }}
        </p>
      </UCard>

      <UCard class="rounded-2xl shadow-sm">
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

      <UCard class="rounded-2xl shadow-sm">
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
      <UCard class="rounded-2xl shadow-sm">
        <CustomCardTitle title="Répartition du patrimoine"/>
        <div class="flex justify-center items-center h-[280px]">
          <DoughnutChart :data="patriomiesDonutChart" :options="optionsChart"/>
        </div>
      </UCard>

      <UCard class="rounded-2xl shadow-sm">
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
        class="rounded-xl shadow-md hover:scale-105 transition-transform"
        @click="openPatrimony()" />
    </div>

    <!-- SECTION : Liste patrimoines -->
    <div class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-4">
      <div 
        v-for="patrimony in patrimonies?.items" 
        :key="patrimony.id"
      >
        <UCard 
          :class="[
            'rounded-2xl transition border hover:shadow-md cursor-pointer',
            patrimony.id === selectedPatrimony?.id ? 'border-pink-500 ring-1 ring-pink-300' : 'border-gray-200'
          ]"
          @click="onClickPatrimony(patrimony.id)"
        >
          <div class="flex items-center justify-between">
            <h5 class="font-semibold text-gray-700">{{ patrimony.title }}</h5>
            <div class="flex gap-1">
              <UButton :disabled="patrimony.id !== selectedPatrimony?.id" 
                icon="i-lucide-plus" size="xs" @click.stop="() => openSnapshot()" />
              <UButton :disabled="patrimony.id !== selectedPatrimony?.id" 
                icon="i-lucide-square-pen" size="xs" @click.stop="openPatrimony(patrimony.id)" />
              <UButton :disabled="patrimony.id !== selectedPatrimony?.id"
                icon="i-lucide-trash" size="xs" @click.stop="deletePatrimony(patrimony.id)" />
            </div>
          </div>

          <UKbd 
            :value="patrimony.type === 'Asset' ? 'Actif' : 'Passif'" 
            class="mt-2"
          />

          <p class="font-bold text-xl mt-4 text-gray-900">
            {{ formatCurrency(patrimony.currentBalance) }}
          </p>

          <p 
            :class="[
              'font-semibold text-md mt-2',
              computeDiff(patrimony.currentBalance, patrimony.lastSnapshotBalance, patrimony.type) > 0 
                ? 'text-green-500' 
                : 'text-red-500'
            ]">
            <span>
              {{ computeDiff(patrimony.currentBalance, patrimony.lastSnapshotBalance, patrimony.type) > 0 ? '+' : '-' }}
            </span>
            {{
              Math.abs(
                roundNumber(
                  computePercentage(
                    patrimony.currentBalance,
                    computeDiff(patrimony.currentBalance, patrimony.lastSnapshotBalance, patrimony.type)
                  )
                )
              )
            }}%
          </p>
        </UCard>
      </div>
    </div>

    <!-- SECTION : Table snapshots -->
    <UCard class="mt-4 rounded-2xl shadow-sm">
      <CustomCardTitle title="Historique des snapshots"/>
      <UTable 
        :columns="columns" 
        :data="snapshots"
        class="mt-2"
      />
    </UCard>
  </div>
</template>