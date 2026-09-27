<script setup lang="ts">
import { API_ROUTES } from '~/shared/routes';
import type { GetSpendingPeriodResponse } from '~/types/api/spending-period';
import { SpendingPeriodType, getClassSpendingPeriodType, getLabelSpendingPeriodType, isInProgressSpendingPeriodType } from '~/types/constants/spendingPeriod';
import { spendingPeriodResponseToSpendingPeriod } from '~/mappers/spending-period';

const { id } = defineProps<{
    id: string
}>()

const emit = defineEmits<{
    close: [doRefresh: boolean]
}>()

const toast = useToast()
const doRefresh = ref(false)
const isCompleting = ref(false)

const { data: period, refresh } = useAsyncData(`spending-period-${id}`, async () => {
    return await ApiLinkBuilder
        .route<GetSpendingPeriodResponse>(API_ROUTES.SPENDING_PERIOD.GET_SPENDING_PERIOD)
        .params({ id })
        .mapper(spendingPeriodResponseToSpendingPeriod)
        .execute()
})

const isReview = computed(() => period.value !== undefined && isInProgressSpendingPeriodType(period.value.state))
const isComplete = computed(() => period.value?.state === SpendingPeriodType.Complete)

async function completePeriod() {
    try {
        isCompleting.value = true

        await ApiLinkBuilder
            .route(API_ROUTES.SPENDING_PERIOD.COMPELETE_SPENDING_PERIOD)
            .params({ id })
            .execute()

        doRefresh.value = true
        await refresh()
    } catch(err: any) {
        toast.add({
            title: "Error spending period",
            description: err?.message,
            color: 'error'
        });
    } finally {
        isCompleting.value = false
    }
}
</script>

<template>
    <USlideover v-on:update:open="emit('close', doRefresh)">
        <template #content>
            <div class="p-6 bg-neutral-50 h-full overflow-auto">
                <div class="flex justify-between items-center mb-3">
                    <span
                        v-if="period"
                        :class="[
                            'px-2.5 py-0.5 font-medium text-[0.70rem] rounded-full',
                            getClassSpendingPeriodType(period.state)
                        ]"
                    >
                        {{ getLabelSpendingPeriodType(period.state) }}
                    </span>
                    <UButton
                        icon="i-lucide-x"
                        variant="ghost"
                        @click="emit('close', doRefresh)"
                    />
                </div>

                <div v-if="period" class="flex flex-col gap-5">
                    <UiSpendingPeriodInfo :period="period" />

                    <UiSpendingPeriodForcast
                        v-if="isReview && period.forcast"
                        :forcast="period.forcast"
                    />

                    <div
                        v-else-if="isReview"
                        class="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 text-gray-400 text-sm"
                    >
                        La prevision de la periode est indisponible.
                    </div>

                    <UiSpendingPeriodSnapshot
                        v-if="isComplete"
                        :snapshot="period.snapshot"
                    />

                    <div v-if="isReview" class="flex justify-end">
                        <UButton
                            icon="i-lucide-check"
                            label="Terminer la periode"
                            :loading="isCompleting"
                            @click="completePeriod"
                        />
                    </div>

                    <div v-else-if="isComplete" class="text-gray-400 text-sm">
                        Cette periode est terminee, les montants du snapshot sont figes.
                    </div>
                </div>

                <div v-else class="flex justify-center py-10">
                    <UIcon name="i-lucide-loader-circle" class="w-6 h-6 animate-spin text-gray-400" />
                </div>
            </div>
        </template>
    </USlideover>
</template>
