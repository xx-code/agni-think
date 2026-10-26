<script setup lang="ts">
import type { NavigationMenuItem } from '@nuxt/ui'

defineProps<{
    items: NavigationMenuItem[]
}>()
</script>

<template>
    <div class="flex flex-col items-center gap-1">
        <LayoutNavbarItem
            v-for="item in items"
            :key="item.label"
            :item="item"
        />
    </div>
</template>