<script lang="ts" setup>
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date';
import { fetchEstimationLeftAmount } from '~/composables/analytics/useGetEstimationLeftAmount';

let startDated = new Date();
let endDated = new Date();

const amount = ref(0)

const startDate = shallowRef(new CalendarDate(startDated.getUTCFullYear(), startDated.getUTCMonth() + 1, startDated.getUTCDate()));
const endDate = shallowRef(new CalendarDate(endDated.getUTCFullYear(), endDated.getUTCMonth() + 1, endDated.getUTCDate()));

const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
});

const toast = useToast();

async function estimation() {
    try {
        const res = await fetchEstimationLeftAmount({ 
            endDate: endDate.value.toString(), 
            startDate: startDate.value.toString() });
        amount.value = res.estimateAmount;
    } catch(err) {
        toast.add({
            title: 'Estimation Error',
            description: 'Error while make estimation',
            color: 'error'
        });
    }
}

</script>

<template>
    <div class="space-y-2 mt-5"> 
        <div class="space-x-3 flex items-center">
            <UPopover>
                <UButton color="neutral" variant="subtle" icon="i-lucide-calendar" >
                    {{ startDate ? df.format(startDate.toDate(getLocalTimeZone()))  : 'Selectionnez une de debut' }}
                </UButton>
                <template #content>
                    <UCalendar v-model="startDate" />
                </template>
            </UPopover>
        
            <UPopover>
                <UButton color="neutral" variant="subtle" icon="i-lucide-calendar" >
                    {{ endDate ? df.format(endDate.toDate(getLocalTimeZone())) : 'Selectionnez une de fin' }}
                </UButton>
                <template #content>
                    <UCalendar v-model="endDate" />
                </template>
            </UPopover>
            
            <UButton
                label="Estimation"
                @click="estimation()"/>
        </div>
        
        <div class="mt-5">
            <div class="font-bold text-xl">{{ formatCurrency(amount) }}</div>
        </div>
    </div>
</template>