<script setup lang="ts">
import type { NuxtError } from '#app'
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'
import { fetchPlanningAdvisorAgent } from '~/composables/agents/usePlanningAdvisorAgent'
import type { PlanningAgentAdvisorType } from '~/types/ui/agent'

export type TargetGoal = {
    goalId: string
    title: string
}

type EditWishSpend = {
    id: number
    amount: number
    description: string
}

const { targetGoals } = defineProps<{
    targetGoals: TargetGoal[]
}>();

const emit = defineEmits<{
    (e: 'close', close: boolean): void
}>();

const isAsked = ref(false);
const isLoading = ref(false);

const comment = ref<string>()
const wishSpends = ref<EditWishSpend[]>([ ])

const date = new Date()
const startDate = shallowRef(new CalendarDate(date.getFullYear(), date.getMonth() + 1, date.getDate()));
const endDate = shallowRef(new CalendarDate(date.getFullYear(), date.getMonth() + 1, date.getDate()));

const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
})

const targetGoalEdits = ref<(TargetGoal & {amount: number, checked: boolean})[]>
    (targetGoals.map(i => ({ checked: true, goalId: i.goalId, title: i.title, amount: 0})))

const agentResponse = ref<PlanningAgentAdvisorType>()

function addWishSpend() {
    wishSpends.value.push({id: wishSpends.value.length, description: '', amount: 0}) 
}
function removeWishSpend(id: number) {
    let index = wishSpends.value.findIndex(i => i.id === id)
    wishSpends.value.splice(index, 1);
}

async function makePlanning() {
    try {
        isAsked.value = true;
        isLoading.value = true;
        const res = await fetchPlanningAdvisorAgent({
            estimationPeriodStart: startDate.value.toDate(getLocalTimeZone()).toISOString(),
            estimationPeriodEnd: endDate.value.toDate(getLocalTimeZone()).toISOString(),
            comment: comment.value || '',
            wishGoals: targetGoalEdits.value.filter(i => i.checked).map(i => ({goalId: i.goalId, amountSuggest: i.amount})),
            wishSpends: wishSpends.value.map(i => ({ amount: i.amount, description: i.description})) 
        });
        agentResponse.value = res; 
        isLoading.value = false
    } catch(err) {
        isAsked.value = false;
        const error = err as NuxtError
        alert("Error! : " + error.message + "\n" +  error.data)
        emit('close', false)
    } 
}

</script>

<template>
    <UModal 
        title="Planning Advisor modal"
        description="Hey &#128075;! Auguste on fait un planning pour la semaine de paye en cours"
        :dismissible="false">
        <template #body> 
            <div v-if="isAsked === false" class="space-y-2">
                <UFormField label="Date de debut" name="startDate">
                    <UPopover>
                        <UButton color="neutral" variant="subtle" icon="i-lucide-calendar" >
                            {{ startDate ? df.format(startDate.toDate(getLocalTimeZone()))  : 'Selectionnez une de debut' }}
                        </UButton>
                        <template #content>
                            <UCalendar v-model="startDate" />
                        </template>
                    </UPopover>
                </UFormField>
                
                <UFormField label="Date de fin" name="endDate">
                    <UPopover>
                        <UButton color="neutral" variant="subtle" icon="i-lucide-calendar" >
                            {{ endDate ? df.format(endDate.toDate(getLocalTimeZone())) : 'Selectionnez une de fin' }}
                        </UButton>
                        <template #content>
                            <UCalendar v-model="endDate" />
                        </template>
                    </UPopover>
                </UFormField>

                <UFormField label="Petit commentaire">
                    <UTextarea v-model="comment" />
                </UFormField>
                <div class="flex flex-row-reverse">
                    <UButton label="Ajouter Depense"  @click="addWishSpend"/>
                </div>

                <div class="space-y-1.5">
                    <div  v-for="wishSpend in wishSpends" :key="wishSpend.id">
                        <div class="flex justify-between items-center">
                            <div class="flex flex-col gap-y-1">
                                <UFormField label="Somme & Detail">
                                    <UInput v-model="wishSpend.amount" type="number"  class="w-25"/> 
                                </UFormField>
                                <UTextarea v-model="wishSpend.description" class="w-100" />
                            </div>
                            <UButton icon="i-lucide-trash" color="error" @click="() => removeWishSpend(wishSpend.id)"/> 
                        </div>
                    </div>
                </div>

                <USeparator label="But cible" /> 

                <div class="space-y-1.5">
                    <div v-for="goal in targetGoalEdits" :key="goal.goalId">
                        <div class="flex items-center" :label="goal.title">
                            <UCheckbox class="mr-2" v-model="goal.checked" />
                            <h4 class="mr-2">{{  goal.title }}: </h4>
                            <UInput v-model="goal.amount" type="number" />
                        </div>
                    </div>
                </div>

                <div class="space-x-2">
                    <UButton label="Goodbye!" color="neutral" @click="emit('close', false)" />
                    <UButton label="Let's Go!" @click="makePlanning" />
                </div>
            </div>

            <div v-else>
                <div v-if="isLoading" class="space-y-3">
                    <div class="animate-pulse space-y-2">
                        <div class="h-4 w-3/4 bg-gray-300 rounded"></div>
                        <div class="h-4 w-1/2 bg-gray-300 rounded"></div>
                    </div>
                    <div class="flex space-x-2 mt-2">
                        <span class="h-2 w-2 bg-gray-400 rounded-full animate-bounce"></span>
                        <span class="h-2 w-2 bg-gray-400 rounded-full animate-bounce delay-150"></span>
                        <span class="h-2 w-2 bg-gray-400 rounded-full animate-bounce delay-300"></span>
                    </div>
                </div>

                <div v-else class="space-y-4">
                    <!-- Commentaire global -->
                    <UCard>
                        <template #header>
                        <h3 class="font-semibold">Conseil général 🎯</h3>
                        </template>
                        <p>{{ agentResponse?.comment }}</p>
                    </UCard>

                    <!-- Suggestions -->
                    <div class="space-y-3">
                        <UCard v-for="goal in agentResponse?.suggestGoalPlanning" :key="goal.saveGoalId">
                        <template #header>
                            <div class="flex items-center justify-between">
                            <h4 class="font-semibold">{{ goal.title }}</h4>
                            <span class="text-sm font-medium bg-primary-100 text-primary-600 px-2 py-0.5 rounded">
                                +{{ goal.amountSuggest }}$
                            </span>
                            </div>
                        </template>
                        <p class="text-gray-600">{{ goal.reason }}</p>
                        </UCard>
                    </div>

                    <div class="space-x-3 space-y-1">
                        <UButton label="Voulez vous reessayer" class="bg-gray-500" @click="isAsked = false"/>
                        <UButton label="On annule tous" color="neutral" @click="emit('close', false)" />
                        <UButton label="On garde la suggestion" @click="() => { }" />
                    </div>
                </div>
            </div>
        </template>
    </UModal>
</template>