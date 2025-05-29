<script setup lang="ts">
import { useListGoals } from "../../composables/goals";
import { EditSavingGoal, EditSavingGoalUpdate } from "#components";

const overlay = useOverlay()
const modalCreateSavingGoal = overlay.create(EditSavingGoal, {
    props: {
    }
})
const modalUpdateAmountSavingGoal = overlay.create(EditSavingGoalUpdate, {
    props: {}
})

const savingGoals = useListGoals()

const onEditSavingGoal = (goal: any | null = null) => {
    if (goal)
        modalCreateSavingGoal.patch({goalId: goal.id, title: goal.title, targetAmount: goal.targetAmount})

    modalCreateSavingGoal.open()
}

const onEditUpdateAmountSavingGoal = (goalId: string, isIncrease: boolean) => {
    modalUpdateAmountSavingGoal.patch({goalId: goalId, isIncrease: isIncrease})
    modalUpdateAmountSavingGoal.open()
}

</script>

<template>
    <div>
       <div class="flex flex-row-reverse" style="margin-top: 1rem;">
            <UButton icon="i-lucide-plus" label="Ajouter un but" size="xl" @click="onEditSavingGoal()"/>
       </div> 
       <div style="margin-top: 1rem;">
            <div class="grid xs:grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-2">
                <div v-for="goal of savingGoals" :key="goal.id">
                    <div class="card-grid" >
                        <CustomCardTitle :title="goal.title">
                            <div class="flex gap-1">
                                <UButton icon="i-lucide-plus" variant="outline" color="neutral" size="xl" @click="onEditUpdateAmountSavingGoal(goal.id, true)"/>
                                <UButton icon="i-lucide-minus" variant="outline" color="neutral" size="xl" @click="onEditUpdateAmountSavingGoal(goal.id, false)"/>
                                <UButton icon="i-lucide-pencil" variant="outline" color="neutral" size="xl" @click="onEditSavingGoal(goal)"/>
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