<script setup lang="ts">
import { ApiLinkBuilder } from '~/utils/ApiLinkBuilder';
import { API_ROUTES } from '~/shared/routes';
import { fundResponseToFund } from '~/mappers/fund';
import { goalResponseToGoal } from '~/mappers/goal';
import { fundCardContextToFundGoalCards } from '~/mappers/fund';
import { goalToGoalForm } from '~/mappers/goal';
import type { ListResponse } from '~/types/api';
import type { GetFundResponse, QueryFilterFundRequest } from '~/types/api/fund';
import type { GoalQueryFilterRequest, GoalResponse } from '~/types/api/goal';
import { GoalType } from '~/types/constants/goal';
import type { FundContext } from '~/types/ui/fund';
import type { GoalForm } from '~/types/form/goal';
import { ModalGoal } from '#components';

const { fundId } = defineProps<{
    fundId: string
}>()
const emit = defineEmits<{
    refresh: [refresh: boolean]
}>()

const isOpen = ref(false)
const isLoading = ref(false)
const toast = useToast() 


const { data:fund, refresh } = useAsyncData(`fundContext+${fundId}${(new Date()).getMilliseconds()}`, async () => {
    isLoading.value = true

    const [resFund, resGoals] = await Promise.all([
        ApiLinkBuilder.route<GetFundResponse>(API_ROUTES.FUNDS.GET_FUND).params({id: fundId}).mapper(fundResponseToFund).execute(),
        (async () => {
            const raw = await ApiLinkBuilder.route<ListResponse<GoalResponse>>(API_ROUTES.GOALS.GET_GOALS).query({ limit: 0, offset: 4, queryAll: true, sourceId: fundId } as GoalQueryFilterRequest).execute()
            return {
                items: raw.items.map(i => goalResponseToGoal(i)),
                total: raw.total
            }
        })()
    ])
    isLoading.value = false


    return {
        ...resFund,
        goals: resGoals.items.map(i => ({
            ...i,
            dueDate: new Date(i.dueDate),
            evaluation: {
                targetAmount: i.targetAmount, 
                currentBalance: i.evaluation.currentBalance,
                percentage: i.evaluation.progressPercentage,
            }
        }))
    } satisfies FundContext
})

const overlay = useOverlay()
const modal = overlay.create(ModalGoal)

async function openGoalModal(goalId?: string) {
    let initData: GoalForm | undefined
    if (goalId) {
        const goal = await ApiLinkBuilder.route<GoalResponse>(API_ROUTES.GOALS.GET_GOAL).params({id: goalId}).mapper(goalResponseToGoal).execute()
        initData = goalToGoalForm(goal)
    }

    modal.open({
        goalId,
        initData,
        type: GoalType.Fund,
        targetSourceId: fundId,
        onClose: () => {
            refresh()
            emit('refresh', true)
        } 
    })
}

async function onClickDeleteGoal(id: string) {
    try {
        await ApiLinkBuilder.route(API_ROUTES.GOALS.DELETE_GOAL).params({id}).execute()
        refresh()
        emit('refresh', true)
    } catch (err: any) {
        toast.add({
            title: 'Error',
            description: err.message,
            color: 'error'
        })
    }
}

</script>

<template>
    <UModal 
        v-model:open="isOpen" 
        :modal="false" :title="`${fund?.title}`"
    >
        <slot />
        <template #content>
            <LoadingIndicator v-if="isLoading" />
            <div v-else-if="fund" class="p-5">
                <div class="flex items-start justify-between mb-4">
                    <div class="flex-1">
                        <h3 class="font-bold text-lg" :title="fund.title">{{ fund.title }}</h3>
                        <p class="text-sm text-neutral-700 tracking-tighter">{{ fund.description }}</p>
                    </div>
                    <UButton 
                        color="neutral"
                        variant="ghost"
                        icon="i-lucide-x"
                        @click="() => {
                            isOpen = false
                        }"
                    />
                </div>

                <UiCardProgress 
                    class="mb-4"
                    :target-amount="fund.target" 
                    :balance="fund.balance"
                    show-percentage
                />

                <div >
                    <div class="flex justify-between items-center mb-2">
                        <h3 class="font-semibold text-lg text-gray-400">Jalons</h3>
                        <UButton 
                            variant="ghost"
                            icon="i-lucide-plus"
                            @click="openGoalModal()"
                        />
                    </div>

                    <div class="flex flex-col gap-2">
                        <UiFundCardGoal 
                            v-for="goal in fundCardContextToFundGoalCards(fund)"
                            :key="goal.id"
                            :goal="goal"
                            @delete="(id: string) => onClickDeleteGoal(id)"
                            @update="(id: string) => openGoalModal(id)"
                        />
                    </div>
                </div>
            </div>
        </template>
    </UModal>
</template>