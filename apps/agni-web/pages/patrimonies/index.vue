<script lang="ts" setup>
import { ModalEditPatrimony, ModalEditSnapshotPatrimony } from '#components';
import type { TableColumn } from '#ui/types';
import { usePatrimonies } from '~/composables/patrimonies/usePatrimonies';
import { fetchPatrimony } from '~/composables/patrimonies/usePatrimony';
import { fetchSnapshotsPatrimony } from '~/composables/patrimonies/useSnapshotsPatrimony';
import type { PatrimonyDetailType, SnapshotPatrimonyType } from '~/types/ui/patrimony';

const {data:patrimonies} = usePatrimonies()


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
        lastBalance: (lastAmountAsset || 0) - (lastAmountLiability || 0)
    } 
})

const totalEpargne = computed(() => {
    return 12000
})

const selectedPatrimony = ref<PatrimonyDetailType>()

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

function openPatrimony(id?: string) {
    
    modalEditPatrimony.open({

    })
}

function openSnapshot(snpashot?: SnapshotPatrimonyType) {
    modalEditSnapshotPatrimony.open({

    })
}

const computeDiff = (currentBalance: number, accountBalance: number) => {
    return currentBalance - accountBalance
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
                        h(UButton, { icon: 'i-lucide-edit', onClick: () => {} }),
                        h(UButton, { icon: 'i-lucide-trash', onClick: () => {} }),
                    ]
                )
            )
        }
    }
]

</script>

<template>
    <div class="space-y-3 mt-3">  
        <div class="flex gap-3">
            <UCard>
                <h4 class="text-sm text-gray-500">Valeur nette total</h4>
                <p class="font-bold text-xl mt-1">{{ formatCurrency(totalPatrimony.currentBalance) }}</p>
            </UCard>
            <UCard>
                <h4 class="text-sm text-gray-500">Evolution mensuelle</h4>
                <p 
                    :class="
                        ['font-semibold text-xl mt-1',
                                computeDiff(totalPatrimony.currentBalance, totalPatrimony.lastBalance) > 0 ?
                                'text-green-500': 'text-red-500'
                        ]"
                    >
                    <span>
                        {{ 
                            computeDiff(totalPatrimony.currentBalance, totalPatrimony.lastBalance) > 0 ?
                            "+" : '-'
                        }}
                    </span>
                    {{
                        Math.abs(roundNumber(computePercentage(totalPatrimony.currentBalance, totalPatrimony.lastBalance)))
                    }}%
                </p>
            </UCard>
            <UCard>
                <h4 class="text-sm text-gray-500">Actif vs Passifs</h4>
                <p class="font-semibold text-xl mt-1">
                    <span>{{
                        Math.abs(roundNumber(computePercentage(totalPatrimony.currentBalance, totalPatrimony.currentAmountAsset)))
                        }}%</span> /
                    <span>{{
                        Math.abs(roundNumber(computePercentage(totalPatrimony.currentBalance, totalPatrimony.currentAmountLiability)))
                        }}</span>%</p>
            </UCard>
            <UCard>
                <h4 class="text-sm text-gray-500">Epargne disponible</h4>
                <p class="font-semibold text-xl mt-1">{{ formatCurrency(totalEpargne) }}</p>
            </UCard>
        </div>

        <div class="flex">
            <UCard>
                <CustomCardTitle title="Répartition des actifs">
                </CustomCardTitle>
                <div class="flex justify-center" style="height: 280px;">
                    <!-- <DoughnutChart :data="budgetChart" :options="optionsChart"/> -->
                </div>
            </UCard>
            <UCard>
                <CustomCardTitle title="Répartition des actifs">
                </CustomCardTitle>
                <div class="flex justify-center" style="height: 280px;">
                    <!-- <DoughnutChart :data="budgetChart" :options="optionsChart"/> -->
                </div>
            </UCard>
        </div>

        <div class="flex flex-row-reverse">
            <UButton 
                label="Ajouter un patrimoine" 
                icon="i-lucide-castle"
                @click="openPatrimony()" />
        </div>

        <div class="flex flex-wrap gap-2">
            <div v-for="patrimony in patrimonies?.items" :key="patrimony.id">
                <UCard 
                    :class="[
                        'w-81 ',
                        patrimony.id === selectedPatrimony?.id ?
                        'border-pink-500 border-1': ''
                    ]" 
                    style="cursor: pointer;"
                    @click="onClickPatrimony(patrimony.id)">
                    <div class="space-y-2">
                        <CustomCardTitle :title="patrimony.title"> 
                            <div class="space-x-2">
                                <UButton 
                                    icon="i-lucide-plus"
                                    @click="() => openSnapshot()" />
                                     
                                <UButton 
                                    icon="i-lucide-square-pen" 
                                    @click="openPatrimony()"/>
                            </div>
                        </CustomCardTitle>
                        <UKbd :value="patrimony.type === 'Asset' ? 'Actif' : 'Passif'" />
                        <p>{{ "" }}</p>
                        
                        <p class="font-semibold text-xl mt-2">{{ formatCurrency(patrimony.currentBalance) }}</p>
                        <p 
                            :class="['font-semibold text-md mt-2',
                                computeDiff(patrimony.currentBalance, patrimony.lastSnapshotBalance) > 0 ?
                                'text-green-500': 'text-red-500'
                            ]">
                            <span>
                                {{ 
                                    computeDiff(patrimony.currentBalance, patrimony.lastSnapshotBalance) > 0 ?
                                    "+" : "-"
                                }}
                            </span>
                            {{ 
                                roundNumber(
                                    computePercentage(patrimony.currentBalance, computeDiff(patrimony.currentBalance, patrimony.lastSnapshotBalance))
                                ) 
                            }}%
                        </p>
                    </div> 
                </UCard>
            </div>
        </div>

        <UCard class="mt-2">
            <UTable 
                :columns="columns" 
                :data="snapshots"
            />
        </UCard>

    </div>
</template>