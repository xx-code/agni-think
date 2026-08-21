<script setup lang="ts">
import type { NuxtError } from '#app';
import { ModalEditSnapshotPatrimony } from '#components';
import { getLocalTimeZone } from '@internationalized/date';
import { fetchPatrimony, fetchSnapshotsPatrimony, useAddSnapshotPatrimony, useRemoveSnapshotPatrimony, useUpdateSnapshotPatrimony } from '~/composables/api/patrimonies';
import { getLabelPatrimonyType } from '~/types/constants/patrimony';
import type { EditSnapshotPatrimony, PatrimonyType, SnapshotPatrimonyType } from '~/types/ui/patrimony';

const { id, isFund } = defineProps<{
    id: string
    isFund?: boolean
}>()

const emit = defineEmits<{
    close: [doRefresh: boolean]
}>()

const isLoading = ref(false) 
const doRefresh = ref(false)
const overlay = useOverlay()
const modalEditSnapshotPatrimony = overlay.create(ModalEditSnapshotPatrimony)

const { data, refresh } = useAsyncData(`patrimony-${id}-${isFund}`, async () => {
    isLoading.value = true
    const resPatrimony = await fetchPatrimony(id, isFund)
    
    let snapshots: SnapshotPatrimonyType[] = []

    const resSnapshots = await fetchSnapshotsPatrimony(id, undefined, undefined, isFund)
    snapshots = resSnapshots.items

    isLoading.value = false

    return {
        patrimony: resPatrimony,
        snapshots
    } 
})

const snaptshotsDate = computed(() => {
    if (!data.value)
        return []

    return Object.assign([] as SnapshotPatrimonyType[], data.value.snapshots).sort((a, b) => a.date.getTime() - b.date.getTime()).map(i => i.date)
})
const snaptshotsBalance = computed(() => {
    if (!data.value)
        return []

    return Object.assign([] as SnapshotPatrimonyType[], data.value.snapshots).sort((a, b) => a.date.getTime() - b.date.getTime()).map(i => i.balance)
})

async function onSubmitSnapshot(snapshot: EditSnapshotPatrimony, oldSnapshot?: SnapshotPatrimonyType) {
    try {
        if (oldSnapshot)
            await useUpdateSnapshotPatrimony(oldSnapshot.patrimonyId, oldSnapshot.id, {
                status: snapshot.status,
                balance: snapshot.balance,
                date: snapshot.date.toDate(getLocalTimeZone()).toISOString()
            })
        else
            await useAddSnapshotPatrimony(id, {
                balance: snapshot.balance,
                status: snapshot.status,
                date: snapshot.date.toDate(getLocalTimeZone()).toISOString()
            })
        doRefresh.value = true
        refresh()
    } catch(err) {
        const nuxtError = err as NuxtError
        alert("Error: " + nuxtError?.cause || 'Error Patrimony')
    }
}

function openSnapshot(snapshot?: SnapshotPatrimonyType) {
    modalEditSnapshotPatrimony.open({
        snapshot: snapshot,
        onSubmit: onSubmitSnapshot
    })
}

async function removeSnapshot(snapshotId: string) {
    await useRemoveSnapshotPatrimony(id, snapshotId)
    refresh()
}



</script>

<template>
    <USlideover v-on:update:open="emit('close', doRefresh)">
        <template #content>
            <div class="p-6 bg-neutral-50 h-full overflow-auto">
                <div class="flex justify-between items-center mb-3">
                    <span 
                        :class="[
                            'px-2.5 py-0.5 font-medium text-[0.70rem]  rounded-full',
                            data?.patrimony.type === 'Asset' ? 'bg-green-500/10 text-green-700' : 'bg-red-500/10 text-red-700'
                        ]" 
                    >
                    {{ getLabelPatrimonyType(data?.patrimony.type ?? 'Asset').toUpperCase()  }}
                    </span>
                    <UButton 
                        icon="i-lucide-x"
                        variant="ghost"
                        @click="emit('close', doRefresh)"
                    />
                </div>

                <div class="flex flex-col gap-5">
                    <SlideOverPatrimonySnapshotInfo  
                        v-if="data"
                        class="mb-4"
                        :title="data.patrimony.title"
                        :balance="data.patrimony.currentBalance"
                        :evolution="data.patrimony.evolution"
                        :type="data.patrimony.type"
                    />

                    <SlideOverPatrimonySnapshotGraph 
                        :net-worth-dates="snaptshotsDate"
                        :net-worth-evolutions="snaptshotsBalance"
                    />

                    <div class="flex items-center">
                        <h2 class="flex-1 text-lg font-semibold">Historique des snapshots</h2>
                        <UButton 
                            :disabled="isFund"
                            icon="i-lucide-plus"
                            label="Snapshot"
                            @click="openSnapshot()"
                        />
                    </div>

                    <div>
                        <div 
                            v-for="(value, index) in data?.snapshots" 
                            :key="value.id"
                            :class="[
                                'flex bg-white p-4 items-center shadow-sm border-dashed border-gray-300',
                                index ==- 0 ? 'rounded-t-2xl': '',
                                index === (data?.snapshots.length ?? 0) - 1 ? 'rounded-b-2xl' : 'border-b'
                            ]">
                            <div class="flex-1">
                                <h3 class="font-semibold">{{ formatCurrency(value.balance) }}</h3>
                                <span class="text-gray-500 text-sm">{{ formatDate(value.date) }}</span>
                            </div>

                            <div class="flex items-center gap-2" :style="{display: isFund ? 'none' : 'flex'}">
                                <span 
                                    :class="[
                                        'px-2.5 py-0.5 font-medium text-[0.70rem]  rounded-full',
                                        value.status.toLowerCase() === 'complete' ? 'bg-green-500/10 text-green-700' : 'bg-gray-500/10 text-gray-700'
                                    ]" 
                                >
                                {{ value.status }}
                                </span>           
                                <UButton 
                                    icon="i-lucide-pen"
                                    variant="ghost"
                                    @click="() => openSnapshot(value)"
                                />
                                <UButton 
                                    icon="i-lucide-trash"
                                    variant="ghost"
                                    color="error"
                                    @click="() => removeSnapshot(value.id)"
                                />
                            </div>
                        </div>
                    </div>

                    <div>
                        <p class="text-gray-400 text-sm">Ce patrimoine se reevalue manuellement. Ajouter un snapshot chaque fois que sa valeur change.</p>
                    </div>
                </div>
            </div> 
        </template>
    </USlideover>
</template>