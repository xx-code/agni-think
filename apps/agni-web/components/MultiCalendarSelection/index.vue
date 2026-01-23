<script setup lang="ts">
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'

const { disabled } = defineProps<{ disabled: boolean}>();

const emit = defineEmits<{
  (e: 'submit', startDate?: CalendarDate, endDate?: CalendarDate): void
}>(); 

const df = new DateFormatter('en-US', {
  dateStyle: 'medium'
});

const today = new Date()

const modelValue = shallowRef({
  start: new CalendarDate(today.getFullYear(), today.getMonth() + 1, 1),
  end: new CalendarDate(today.getFullYear(), today.getMonth() + 1, 5)
});

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
        Pick a date
      </template>
    </UButton>

    <template #content>
      <UCalendar 
        v-model="modelValue" 
        class="p-2" 
        range 
        v-on:update:model-value="emit('submit', modelValue.start, modelValue.end)"/>
    </template>
  </UPopover>
</template>

