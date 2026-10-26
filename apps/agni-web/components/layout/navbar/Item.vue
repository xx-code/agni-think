<script setup lang="ts">
import type { NavigationMenuItem } from '@nuxt/ui'

const props = defineProps<{
    item: NavigationMenuItem
}>()

const route = useRoute()

const hasChildren = computed(() => !!props.item.children?.length)

function isPathActive(to?: NavigationMenuItem['to']) {
    if (typeof to !== 'string') return false
    return to === '/' ? route.path === '/' : route.path === to || route.path.startsWith(`${to}/`)
}

const isActive = computed(() => isPathActive(props.item.to))
const isGroupActive = computed(() => !!props.item.children?.some(child => isPathActive(child.to)))

// Shared look: a square icon button, gray rounded background when active
const buttonClass = (active: boolean) => [
    'group flex size-9 items-center justify-center rounded-lg transition-colors duration-200 text-lg',
    active ? 'bg-neutral-100 text-neutral-900' : 'text-neutral-400 hover:bg-neutral-50 hover:text-neutral-700'
]
</script>

<template>
    <!-- Leaf item: icon only, label shown in a tooltip -->
    <UTooltip
        v-if="!hasChildren"
        :text="item.label"
        :content="{ side: 'right', sideOffset: 10 }"
    >
        <ULink
            :to="item.to"
            :aria-label="item.label"
            :class="buttonClass(isActive)"
        >
            <UIcon :name="item.icon!" class="shrink-0" />
        </ULink>
    </UTooltip>

    <!-- Parent item: icon opens a flyout with the children -->
    <UPopover
        v-else
        mode="hover"
        :open-delay="100"
        :content="{ side: 'right', align: 'start', sideOffset: 10 }"
    >
        <button
            type="button"
            :aria-label="item.label"
            :class="buttonClass(isGroupActive)"
        >
            <UIcon :name="item.icon!" class="size-[18px] shrink-0" />
        </button>

        <template #content>
            <div class="flex w-44 flex-col gap-0.5 p-1.5">
                <p class="px-2 py-1 text-xs font-medium text-neutral-400">{{ item.label }}</p>
                <ULink
                    v-for="child in item.children"
                    :key="child.to as string"
                    :to="child.to"
                    class="flex h-8 items-center gap-2.5 rounded-md px-2 text-[13px] transition-colors"
                    :class="isPathActive(child.to)
                        ? 'bg-neutral-100 font-medium text-neutral-900'
                        : 'text-neutral-600 hover:bg-neutral-50'"
                >
                    <UIcon v-if="child.icon" :name="child.icon" class="size-4 shrink-0" />
                    <span class="truncate">{{ child.label }}</span>
                </ULink>
            </div>
        </template>
    </UPopover>
</template>