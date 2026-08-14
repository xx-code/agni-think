<script setup lang="ts">
import type { NavigationToggleItem } from '~/types/ui/navigation';

const activeId = defineModel<string>()

const { items } = defineProps<{
    items: NavigationToggleItem[] 
}>()

</script>

<template>
    <div class="flex gap-1 p-1 rounded-[12px] border border-solid border-neutral-300 w-fit items-center">
        <UButton
            v-for="item in items"
            :icon="item.iconName"
            :label="item.title"
            :variant="activeId == item.id ? 'solid' : 'outline'"
            color="neutral"
            size="lg"
            @click="() => { activeId = item.id }"
        />
    </div>
</template>