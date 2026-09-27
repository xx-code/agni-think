<script setup lang="ts">
import { SlideOverSpendingPeriod } from "#components"
import { useSpendingPeriodModal } from '~/composables/modal/spendingPeriod'
import useConfirmModal from '~/composables/modal/useConfirmModal'
import { listSpendingPeriodsResponseToListSpendingPeriods, spendingPeriodResponseToSpendingPeriod } from '~/mappers/spending-period'
import { API_ROUTES } from '~/shared/routes'
import type { ListResponse } from '~/types/api'
import type { GetAllSpendingPeriodResponse, GetSpendingPeriodResponse } from '~/types/api/spending-period'
import { SpendingPeriodType, getClassSpendingPeriodType, getLabelSpendingPeriodType, isInProgressSpendingPeriodType } from '~/types/constants/spendingPeriod'
import type { AllSpendingPeriod, SpendingPeriod } from '~/types/ui/spending-period'

const toast = useToast()
const overlay = useOverlay()
const slideOverSpendingPeriod = overlay.create(SlideOverSpendingPeriod)
const { open: openEditPeriod } = useSpendingPeriodModal(overlay)
const { open: openConfirm } = useConfirmModal(overlay)

const periods = ref<AllSpendingPeriod[]>([])
const totalPeriods = ref(0)
const isLoading = ref(false)
const isLoadingInProgress = ref(false)
const completingId = ref<string | undefined>(undefined)
const deletingId = ref<string | undefined>(undefined)
const selectedState = ref<SpendingPeriodType | undefined>(SpendingPeriodType.Complete)
const inProgressPeriod = ref<SpendingPeriod | undefined>(undefined)

// In progress periods come first, then drafts, then completed ones
const stateOrder = [SpendingPeriodType.InProgress, SpendingPeriodType.ToReview, SpendingPeriodType.Draft, SpendingPeriodType.Complete]

const sortedPeriods = computed(() => {
    return Object.assign([] as AllSpendingPeriod[], periods.value).sort((a, b) => stateOrder.indexOf(a.state) - stateOrder.indexOf(b.state))
})

const stateFilters = computed(() => {
    const states = [SpendingPeriodType.Draft, SpendingPeriodType.ToReview, SpendingPeriodType.Complete].map(state => ({
        label: getLabelSpendingPeriodType(state),
        value: state,
        count: periods.value.filter(i => i.state === state).length
    })) 

    states.push({
        label: 'Toutes',
        //@ts-ignore
        value: undefined,
        count: totalPeriods.value
    })

    return states
})

const displayedPeriods = computed(() => {
    const others = sortedPeriods.value.filter(i => i.id !== inProgressPeriod.value?.id)

    if (selectedState.value === undefined)
        return others

    return others.filter(i => i.state === selectedState.value)
})

async function getInProgressPeriod() {
    const period = sortedPeriods.value.find(i => isInProgressSpendingPeriodType(i.state))

    if (!period) {
        inProgressPeriod.value = undefined
        return
    }

    isLoadingInProgress.value = true
    try {
        inProgressPeriod.value = await ApiLinkBuilder
            .route<GetSpendingPeriodResponse>(API_ROUTES.SPENDING_PERIOD.GET_SPENDING_PERIOD)
            .params({ id: period.id })
            .mapper(spendingPeriodResponseToSpendingPeriod)
            .execute()
    } catch {
        inProgressPeriod.value = undefined
    } finally {
        isLoadingInProgress.value = false
    }
}

async function getAllSpendingPeriods() {
    isLoading.value = true
    try {
        const res = await ApiLinkBuilder
            .route<ListResponse<GetAllSpendingPeriodResponse>>(API_ROUTES.SPENDING_PERIOD.GET_ALL_SPENDING_PERIOD)
            .query({
                'queryFilter.offset': 0,
                'queryFilter.limit': 0,
                'queryFilter.queryAll': true,
                'queryFilter.sortBy.by': 'start_date',
                'queryFilter.sortBy.ascending': false
            })
            .mapper(listSpendingPeriodsResponseToListSpendingPeriods)
            .execute()

        periods.value = res.items
        totalPeriods.value = res.total

        await getInProgressPeriod()
    } catch(err: any) {
        toast.add({
            title: 'Erreur Periodes de depenses',
            description: err?.message,
            color: 'error'
        })
    } finally {
        isLoading.value = false
    }
}

function onOpenPeriod(id: string) {
    slideOverSpendingPeriod.open({
        id,
        onClose: (doRefresh: boolean) => {
            if (doRefresh)
                getAllSpendingPeriods()
        }
    })
}

function onUpdatePeriod(id: string) {
    openEditPeriod(getAllSpendingPeriods, id)
}

function onCompletePeriod(id: string) {
    openConfirm({
        title: 'Terminer la periode',
        description: 'Le snapshot sera fige avec les depenses reelles de la periode. Cette action est irreversible.',
        confirmLabel: 'Terminer'
    }, () => completePeriod(id))
}

function onDeletePeriod(id: string) {
    openConfirm({
        title: 'Supprimer la periode',
        description: 'La periode en cours sera supprimee definitivement.',
        confirmLabel: 'Supprimer',
        variant: 'danger'
    }, () => deletePeriod(id))
}

async function deletePeriod(id: string) {
    try {
        deletingId.value = id

        await ApiLinkBuilder
            .route(API_ROUTES.SPENDING_PERIOD.DELETE_SPENDING_PERIOD)
            .params({ id })
            .execute()

        toast.add({
            title: 'Succès',
            description: 'Periode supprimee',
            color: 'success'
        })

        await getAllSpendingPeriods()
    } catch(err: any) {
        toast.add({
            title: 'Erreur Periode',
            description: err?.message,
            color: 'error'
        })
    } finally {
        deletingId.value = undefined
    }
}

async function completePeriod(id: string) {
    try {
        completingId.value = id

        await ApiLinkBuilder
            .route(API_ROUTES.SPENDING_PERIOD.COMPELETE_SPENDING_PERIOD)
            .params({ id })
            .execute()

        toast.add({
            title: 'Succès',
            description: 'Periode terminee',
            color: 'success'
        })

        await getAllSpendingPeriods()
    } catch(err: any) {
        toast.add({
            title: 'Erreur Periode',
            description: err?.message,
            color: 'error'
        })
    } finally {
        completingId.value = undefined
    }
}

await useAsyncData('page-spending-periods', async () => {
    await getAllSpendingPeriods()

    return true
})
</script>

<template>
    <UiPage>
        <UiPageHeader
            title="Periodes de depenses"
            subtitle="Suivre vos periodes, vos previsions et vos snapshots"
        />

        <!-- In progress period -->
        <div v-if="isLoading || isLoadingInProgress" class="flex justify-center py-10">
            <UIcon name="i-lucide-loader-circle" class="w-6 h-6 animate-spin text-gray-400" />
        </div>

        <div v-else-if="inProgressPeriod" class="space-y-4">
            <div class="flex items-center justify-between flex-wrap gap-2">
                <div class="flex items-center gap-2">
                    <UIcon name="i-lucide-play-circle" class="w-5 h-5 text-blue-500" />
                    <h2 class="text-lg font-semibold text-gray-900">Periode en cours</h2>
                    <span :class="[
                        'px-2.5 py-0.5 font-medium text-[0.70rem] rounded-full',
                        getClassSpendingPeriodType(inProgressPeriod.state)
                    ]">
                        {{ getLabelSpendingPeriodType(inProgressPeriod.state) }}
                    </span>
                </div>

                <div class="flex items-center gap-2">
                    <UButton
                        v-if="inProgressPeriod.state === SpendingPeriodType.ToReview"
                        icon="i-lucide-check"
                        label="Terminer la periode"
                        :loading="completingId === inProgressPeriod.id"
                        @click="onCompletePeriod(inProgressPeriod.id)"
                    />

                    <UButton
                        icon="i-lucide-trash-2"
                        color="error"
                        variant="outline"
                        label="Supprimer"
                        :loading="deletingId === inProgressPeriod.id"
                        @click="onDeletePeriod(inProgressPeriod.id)"
                    />
                </div>
            </div>

            <UiSpendingPeriodInfo :period="inProgressPeriod" />

            <UiSpendingPeriodForcast
                v-if="inProgressPeriod.forcast"
                :forcast="inProgressPeriod.forcast"
            />

            <div
                v-else
                class="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 text-gray-400 text-sm"
            >
                La prevision de la periode est indisponible.
            </div>
        </div>

        <div v-else-if="!isLoading" class="bg-white rounded-2xl shadow-sm border border-dashed border-gray-200 p-5 text-gray-400 text-sm">
            Aucune periode en cours.
        </div>

        <!-- State filters -->
        <div class="flex items-center gap-2 flex-wrap">
            <UButton
                v-for="stateFilter in stateFilters"
                :key="stateFilter.label"
                :label="`${stateFilter.label} (${stateFilter.count})`"
                :variant="selectedState === stateFilter.value ? 'solid' : 'outline'"
                :color="selectedState === stateFilter.value ? 'primary' : 'neutral'"
                @click="() => { selectedState = stateFilter.value }"
            />
        </div>

        <!-- Spending periods -->
        <div v-if="isLoading" class="flex justify-center py-10">
            <UIcon name="i-lucide-loader-circle" class="w-6 h-6 animate-spin text-gray-400" />
        </div>

        <div v-else class="grid grid-cols-1 gap-4 sm:grid-cols-[repeat(auto-fill,minmax(280px,1fr))] md:grid-cols-[repeat(auto-fill,minmax(340px,1fr))] md:gap-6">
            <TransitionGroup name="spending-period-list">
                <UiSpendingPeriodCard
                    v-for="period in displayedPeriods"
                    :key="period.id"
                    :period="period"
                    @open="(id) => onOpenPeriod(id)"
                    @update="(id) => onUpdatePeriod(id)"
                    @complete="(id) => onCompletePeriod(id)"
                />
            </TransitionGroup>

            <div
                v-if="displayedPeriods.length === 0"
                class="col-span-full py-16 text-center"
            >
                <UIcon name="i-lucide-calendar-range" class="w-12 h-12 mx-auto text-gray-300 mb-3" />
                <h3 class="font-semibold text-gray-700">
                    {{ selectedState === undefined ? 'Aucune periode de depense' : 'Aucune periode dans cet etat' }}
                </h3>
                <p class="text-gray-400 text-sm mt-1">
                    Les periodes sont creees automatiquement a partir de vos modeles de periode.
                </p>
            </div>
        </div>
    </UiPage>
</template>
