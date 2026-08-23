<script setup lang="ts">
import type { NavigationMenuItem } from '@nuxt/ui'

const props = defineProps<{
    item: NavigationMenuItem
    depth?: number
    open?: boolean
}>()

const emit = defineEmits<{ toggle: [] }>()

const route = useRoute()

const hasChildren = computed(() => !!props.item.children?.length)
const isActive = computed(() => props.item.to && route.path === props.item.to)
const isGroupActive = computed(() =>
  props.item.children?.some(child => child.to === route.path)
)
</script>

<template>
  <!-- Leaf item -->
    <ULink
        v-if="!hasChildren"
        :to="item.to"
        class="group flex items-center rounded-lg transition-colors"
        :class="depth
        ? [
            'hidden lg:flex lg:ml-4 lg:h-8 lg:gap-2.5 lg:border-l-2 lg:pl-2.5 lg:px-2.5',
            isActive ? 'lg:border-primary-600 lg:bg-primary-50 lg:text-primary-700' : 'lg:border-neutral-200 lg:text-neutral-500 lg:hover:text-neutral-900'
        ]
        : [
            // Compact (< lg): icon on top, xs label truncated below
            'h-14 w-full flex-col justify-center gap-1 px-1',
            // Full (>= lg): icon + label side by side
            'lg:h-9 lg:flex-row lg:justify-start lg:gap-2.5 lg:px-2.5',
            isActive ? 'bg-primary-50 text-primary-700' : 'text-neutral-700 hover:bg-neutral-50'
        ]"
    >
        <UIcon
            :name="item.icon!"
            class="shrink-0"
            :class="[
                depth ? 'lg:size-4' : 'size-5 lg:size-[18px]',
                isActive ? 'text-primary-600' : 'text-neutral-400 group-hover:text-neutral-600'
            ]"
        />
        <span
        class="truncate text-wrap text-center w-8" :title="item.label"
        :class="[
            depth ? 'hidden lg:block lg:w-auto lg:text-start lg:text-[13px]' : 'text-[11px] leading-none lg:w-auto lg:text-start lg:text-sm',
            isActive ? 'font-medium' : 'font-normal'
        ]"
        >
            {{ item.label }}
        </span>
    </ULink>

    <template v-else>
        <!-- Compact (< lg): icon rail trigger opens a flyout -->
        <UPopover class="lg:hidden" :content="{ side: 'right', align: 'start', sideOffset: 8 }">
            <button
                type="button"
                class="flex h-14 w-full flex-col items-center justify-center gap-1 rounded-lg px-1 transition-colors"
                :class="isGroupActive ? 'bg-primary-50 text-primary-700' : 'text-neutral-700 hover:bg-neutral-50'"
            >
                <UIcon
                :name="item.icon!"
                class="size-5 shrink-0"
                :class="isGroupActive ? 'text-primary-600' : 'text-neutral-400'"
                />
                <span class="w-full wrap-break-word truncate text-center text-[11px] leading-none">{{ item.label }}</span>
            </button>

            <template #content>
                <div class="flex w-44 flex-col gap-0.5 p-1.5">
                    <p class="px-2 py-1 text-xs font-medium text-neutral-400">{{ item.label }}</p>
                    <ULink
                        v-for="child in item.children"
                        :key="child.to"
                        :to="child.to"
                        class="flex h-8 items-center gap-2.5 rounded-md px-2 text-[13px] transition-colors"
                        :class="route.path === child.to ? 'bg-primary-50 text-primary-700 font-medium' : 'text-neutral-600 hover:bg-neutral-50'"
                    >
                        <UIcon :name="child.icon!" class="size-4 shrink-0" />
                        <span class="truncate">{{ child.label }}</span>
                    </ULink>
                </div>
            </template>
        </UPopover>

        <!-- Parent item with children -->
        <UCollapsible :open="open" class="hidden lg:block" @update:open="emit('toggle')">
            <UButton
                block
                variant="ghost"
                color="neutral"
                class="h-9 justify-start gap-2.5 px-2.5 hover:bg-neutral-50"
                :class="isGroupActive ? 'text-primary-700' : 'text-neutral-700'"
                :aria-expanded="open"
            >
                <UIcon
                    :name="item.icon!"
                    class="size-[18px] shrink-0"
                    :class="isGroupActive ? 'text-primary-600' : 'text-neutral-400'"
                />
                <span class="flex-1 truncate text-start text-sm font-medium">{{ item.label }}</span>
                <UIcon
                    name="i-lucide-chevron-down"
                    class="size-3.5 shrink-0 text-neutral-400 transition-transform duration-200"
                    :class="{ 'rotate-180': open }"
                />
            </UButton>

            <template #content>
                <div class="mt-0.5 flex flex-col gap-0.5 py-0.5">
                    <LayoutNavbarItem
                        v-for="child in item.children"
                        class="rounded-none"
                        :key="child.to"
                        :item="child"
                        :depth="(depth || 0) + 1"
                    />
                </div>
            </template>
        </UCollapsible>
    </template> 
</template>