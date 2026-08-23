<script setup lang="ts">
import type { NavigationMenuItem } from '@nuxt/ui'

const props = defineProps<{
    items: NavigationMenuItem[]
    divider?: boolean
}>()

const route = useRoute()

const openLabel = ref(
    props.items.find(item => item.children?.some(child => child.to === route.path))?.label
)

function toggle(label?: string) {
    openLabel.value = openLabel.value === label ? undefined : label
}
</script>

<template>
    <div
        class="flex flex-col gap-0.5"
        :class="divider ? 'mt-2 border-t border-neutral-200 pt-2' : ''"
    >
        <LayoutNavbarItem
            v-for="item in items"
            :key="item.label"
            :item="item"
            :open="openLabel === item.label || item.open"
            @toggle="toggle(item.label)"
        />
    </div>
</template>