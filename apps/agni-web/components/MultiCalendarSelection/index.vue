<script setup lang="ts">
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'

const { 
    disabled, 
    initialStartDate,
    initialEndDate } = defineProps<{ 
    disabled?: boolean
    initialStartDate?: Date
    initialEndDate?: Date
}>();

const emit = defineEmits<{
    (e: 'submit', startDate?: CalendarDate, endDate?: CalendarDate): void
}>(); 

const df = new DateFormatter('en-US', {
    dateStyle: 'medium'
});

const modelValue = shallowRef({
    start: initialStartDate ? new CalendarDate(initialStartDate.getFullYear(), initialStartDate.getMonth() + 1, initialStartDate.getDate()) : undefined,
    end: initialEndDate ? new CalendarDate(initialEndDate.getFullYear(), initialEndDate.getMonth() + 1, initialEndDate.getDate()) : undefined
});

function clean() {
    modelValue.value = {
        start: undefined,
        end: undefined
    }
    emit("submit", undefined, undefined)
}

</script>

<template>
    <UPopover>
        <UButton :disabled="disabled" color="neutral" variant="subtle" icon="i-lucide-calendar">
        <template v-if="modelValue.start">
            <template v-if="modelValue.end">
                {{ df.format(modelValue.start.toDate(getLocalTimeZone())) }} - {{ df.format(modelValue.end.toDate(getLocalTimeZone())) }}
            </template>

            <template v-else>
                {{ df.format(modelValue.start.toDate(getLocalTimeZone())) }}
            </template>
        </template>
        <template v-else>
            Choisir une date
        </template>
        </UButton>

        <template #content>
            <div>
                <UCalendar 
                    v-model="modelValue" 
                    class="p-2" 
                    range 
                    v-on:update:model-value="emit('submit', modelValue.start, modelValue.end)"/>
                <div class="flex justify-end p-2">
                    <UButton 
                        label="Nettoyer" 
                        variant="ghost"
                        @click="clean()"
                    />
                </div>
            </div>
        </template>
    </UPopover>
</template>

