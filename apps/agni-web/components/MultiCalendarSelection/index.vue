<script setup lang="ts">
import { CalendarDate, DateFormatter, getLocalTimeZone, toCalendar, toCalendarDate } from '@internationalized/date'
import type { MultiCalendarSelection } from '~/types/ui/component';

const { disabled } = defineProps<{ 
    disabled?: boolean
}>();

const df = new DateFormatter('en-US', {
    dateStyle: 'medium'
});

const modelValue = defineModel<MultiCalendarSelection>();

function clean() {
    modelValue.value = {
        start: undefined,
        end: undefined
    }
}

</script>

<template>
    <UPopover v-if="modelValue">
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
                <!--@vue-ignore-->
                <UCalendar 
                    v-model="modelValue" 
                    class="p-2" 
                    range 
                />
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

