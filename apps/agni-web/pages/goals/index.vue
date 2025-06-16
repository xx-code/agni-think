<script setup lang="ts">
import { fetchDeleteSaveGoal, fetchListGoal, useFetchListGoals } from "../../composables/goals";
import { EditSavingGoal, EditSavingGoalUpdate, UBadge, UButton, UInput, UPopover } from "#components";
import { fetchDeleteAccount, fetchListAccounts } from "../../composables/account";
import { ref } from "vue";

const {data: goals, error: errorGoals, refresh: refreshGoals} =  useFetchListGoals()

const overlay = useOverlay()
const modalCreateSavingGoal = overlay.create(EditSavingGoal, {
    props: {
        onSaved: async () => {
            refreshGoals()
        }
    }
})
const modalUpdateAmountSavingGoal = overlay.create(EditSavingGoalUpdate, {
    props: {
        onSaved: async () => {
            refreshGoals()
        }
    }
})


const onEditSavingGoal = (goalId: string |null = null) => {
    if (goalId)
        modalCreateSavingGoal.patch({goalId: goalId, isEdit: true})
    else 
        modalCreateSavingGoal.patch({goalId: '', isEdit: false})

    modalCreateSavingGoal.open()
}

const onEditUpdateAmountSavingGoal = (goalId: string, isIncrease: boolean) => {
    modalUpdateAmountSavingGoal.patch({goalId: goalId, isIncrease: isIncrease})
    modalUpdateAmountSavingGoal.open()
}

const {data: accounts, error: errorAccount, refresh: refreshAccounts } = useFetchListGoals()

const deleteAccountDepositId = ref('')
const deletePopOverOpen = ref(false)

const onDeleteGoal = async (goalId: string) => {
    if (deleteAccountDepositId.value !== '') {
        await fetchDeleteSaveGoal(goalId, deleteAccountDepositId.value)
        await refreshGoals()
        deletePopOverOpen.value = false
    }
}

</script>

<template>
    <div>
       <div class="flex flex-row-reverse" style="margin-top: 1rem;">
            <UButton icon="i-lucide-plus" label="Ajouter un but" size="xl" @click="onEditSavingGoal()"/>
       </div> 
       <div style="margin-top: 1rem;">
            <div class="grid xs:grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-2">
                <div v-for="goal of goals" :key="goal.id">
                    <div class="card-grid" >
                        <CustomCardTitle :title="goal.title">
                            <div class="flex gap-1">
                                <UButton icon="i-lucide-plus" variant="outline" color="neutral" size="xl" @click="onEditUpdateAmountSavingGoal(goal.id, true)"/>
                                <UButton icon="i-lucide-minus" variant="outline" color="neutral" size="xl" @click="onEditUpdateAmountSavingGoal(goal.id, false)"/>
                                <UButton icon="i-lucide-pencil" variant="outline" color="neutral" size="xl" @click="onEditSavingGoal(goal.id)"/>
                                <UPopover v-model="deletePopOverOpen">
                                    <UButton icon="i-lucide-trash" variant="outline" color="neutral" size="xl" @click="deletePopOverOpen = true"/>
                                    <template #content>
                                        <div class="flex flex-col gap-2 p-2">
                                            <USelect v-model="deleteAccountDepositId" value-key="id" label-key="title" :items="accounts" class="w-full" />
                                            <UButton label="Suppmier" color="error" @click="onDeleteGoal(goal.id)"/>
                                        </div>
                                    </template>
                                </UPopover>
                            </div>
                        </CustomCardTitle>
                        <div class="flex items-center" style="margin-top: 1rem;">
                            <AmountTitle :amount="goal.amount" /> 
                            <p class="text-2xl" style="font-weight: bold;color: #D9D9D9;">/</p>
                            <p style="color: #6755D7;">
                                ${{ goal.targetAmount }}
                            </p>
                        </div>
                        <div style="margin-top: 1rem;">
                            <UProgress v-model="goal.amount" :max="goal.targetAmount" size="xl"/>
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