<script setup lang="ts">
import { 
    ModalEditAmountSaveGoal, 
    ModalEditSaveGoal, 
    UButton, 
    UPopover } from "#components";
import { DateFormatter } from "@internationalized/date";
import { ref } from "vue";
import useAccounts from "~/composables/accounts/useAccounts";
import useCreateSaveGoal from "~/composables/goals/useCreateSaveGoal";
import useDeleteSaveGoal from "~/composables/goals/useDeleteSaveGoal";
import { fetchSaveGoal } from "~/composables/goals/useSaveGoal";
import useSaveGoals from "~/composables/goals/useSaveGoals";
import useUpdateAmountSaveGoal from "~/composables/goals/useUpdateAmountSaveGoal";
import useUpdateSaveGaol from "~/composables/goals/useUpdateSaveGoal";
import useImportanceTypes from "~/composables/internals/useImportanceTypes";
import useIntensityDesirTypes from "~/composables/internals/useImportanceTypes";
import type { EditSaveGoalType, EditUpdateAmountSaveGoalType, SaveGoalType } from "~/types/ui/saveGoal";

const {data: goals, error: errorGoals, refresh: refreshGoals} =  useSaveGoals()

const toast = useToast();
const overlay = useOverlay();
const modalCreateSavingGoal = overlay.create(ModalEditSaveGoal);
const modalUpdateAmountSavingGoal = overlay.create(ModalEditAmountSaveGoal);

const { data:intensityDesirs } = useIntensityDesirTypes();
const { data:importances } = useImportanceTypes()

const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
});

async function onSubmitSaveGoal(value: EditSaveGoalType, oldValue?: SaveGoalType) {
    try {
        if (oldValue)
            useUpdateSaveGaol(oldValue.id, {
                title: value.title,
                description: value.description,
                target: value.target,
                importance: value.importance,
                desirValue: value.desirValue,
                wishDueDate: value.wishDueDate?.toString()
            });
        else 
            useCreateSaveGoal({
                title: value.title,
                description: value.description,
                target: value.target,
                importance: value.importance,
                desirValue: value.desirValue,
                wishDueDate: value.wishDueDate?.toString(),
                items: []
            });
        refreshGoals();
    } catch(err) {
        toast.add({
            title: 'Error submition save goal',
            description: "Error while submit save goal " + err,
            color: 'error'
        })
    }
} 


async function openSavingGoal(goalId?: string) {
    let goal: SaveGoalType|undefined = undefined;
    if (goalId) {
        goal = await fetchSaveGoal(goalId); 
    }

    modalCreateSavingGoal.open({
        saveGoal: goal,
        onSubmit: onSubmitSaveGoal 
    }); 
};

async function onUpdateAmountSaveGoal(value: EditUpdateAmountSaveGoalType, isIncrease: boolean, oldValue?: SaveGoalType) {
    try {
        if (oldValue)
            await useUpdateAmountSaveGoal({
                isIncrease: isIncrease,
                amount: value.amount,
                accountId: value.accountId,
                saveGoalId: oldValue?.id
            });

        refreshGoals()
    } catch(err) {
        toast.add({
            title: 'Error Update amount save goal',
            description: "Error while update save goal " + err,
            color: 'error'
        })
    }
}

const openUpdateAmountSavingGoal = async (isIncrease: boolean, goalId?: string) => {
    let goal: SaveGoalType|undefined = undefined;
    if (goalId) {
        goal = await fetchSaveGoal(goalId); 
    }
    
    modalUpdateAmountSavingGoal.open({
        isIncrease: isIncrease,
        onSubmit: onUpdateAmountSaveGoal,
        saveGoal: goal
    }); 
};

const {data: accounts, error: errorAccount, refresh: refreshAccounts } = useAccounts()

const deleteAccountDepositId = ref('')
const deletePopOverOpen = ref(false)

const onDeleteGoal = async (goalId: string) => {
    if (deleteAccountDepositId.value !== '') {
        await useDeleteSaveGoal(goalId, { accountDepositId: deleteAccountDepositId.value });
        await refreshGoals();
        deletePopOverOpen.value = false
    }
}

</script>

<template>
    <div>
       <div class="flex flex-row-reverse" style="margin-top: 1rem;">
            <UButton icon="i-lucide-plus" label="Ajouter un but" size="xl" @click="openSavingGoal()"/>
       </div> 
       <div style="margin-top: 1rem;">
            <div class="grid xs:grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-2">
                <div v-for="goal of goals?.items" :key="goal.id">
                    <div class="card-grid" >
                        <CustomCardTitle :title="goal.title">
                            <div class="flex gap-1">
                                <UButton icon="i-lucide-plus" variant="outline" color="neutral" size="xl" @click="openUpdateAmountSavingGoal(true, goal.id)"/>
                                <UButton icon="i-lucide-minus" variant="outline" color="neutral" size="xl" @click="openUpdateAmountSavingGoal(false, goal.id)"/>
                                <UButton icon="i-lucide-pencil" variant="outline" color="neutral" size="xl" @click="openSavingGoal(goal.id)"/>
                                <UPopover v-model="deletePopOverOpen">
                                    <UButton icon="i-lucide-trash" variant="outline" color="neutral" size="xl" @click="deletePopOverOpen = true"/>
                                    <template #content>
                                        <div class="flex flex-col gap-2 p-2">
                                            <USelect 
                                                v-model="deleteAccountDepositId" 
                                                value-key="value" label-key="label" 
                                                :items="accounts?.items.map(i => ({ label: i.title, value: i.id}))" class="w-full" />
                                            <UButton label="Suppmier" color="error" @click="onDeleteGoal(goal.id)"/>
                                        </div>
                                    </template>
                                </UPopover>
                            </div>
                        </CustomCardTitle>
                        <div class="flex items-center" style="margin-top: 1rem;">
                            <AmountTitle :amount="goal.balance" /> 
                            <p class="text-2xl" style="font-weight: bold;color: #D9D9D9;">/</p>
                            <p style="color: #6755D7;">
                                ${{ goal.target }}
                            </p>
                        </div>
                        <div>
                            {{ goal.wishDueDate ? df.format(new Date(goal.wishDueDate)) : ''}}
                        </div>
                        <div style="margin-top: 1rem;">
                            <UProgress v-model="goal.balance" :max="goal.target" size="xl"/>
                        </div>
                    </div>
                </div>
            </div>
       </div>
    </div>    
</template>

<style lang="scss">
.card-grid {
    border: solid 1px #E6E6E6;
    padding: 0.5rem;
}
</style>