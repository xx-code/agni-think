<script lang="ts" setup>
import type { ConfirmDialog } from '~/types/ui/confirm';

const props = withDefaults(defineProps<{
    data: ConfirmDialog
    loading?: boolean
}>(), {
    loading: false
})


const emit = defineEmits<{
    close: [isConfirm: boolean]
}>()


const isDanger = computed(() => props.data.variant === 'danger')


</script>

<template>
    <UModal>
        <template #content>
            <div class="p-6 flex flex-col gap-5">
                <div class="flex items-start gap-3">
                    <div
                        class="flex items-center justify-center rounded-full shrink-0 size-10"
                        :class="isDanger
                            ? 'bg-red-100 dark:bg-red-500/10'
                            : 'bg-primary-100 dark:bg-primary-500/10'"
                    >
                        <UIcon
                            :name="isDanger ? 'i-lucide-triangle-alert' : 'i-lucide-circle-help'"
                            class="text-xl"
                            :class="isDanger ? 'text-red-500' : 'text-primary-500'"
                        />
                    </div>

                    <div class="flex-1 min-w-0 pt-1">
                        <h2 class="text-base font-semibold text-highlighted">{{ data.title }}</h2>
                        <p class="text-sm text-neutral-500 dark:text-neutral-400 mt-1">
                            {{ data.description }}
                        </p>
                    </div>
                </div>

                <div class="flex justify-end gap-2">
                    <UButton
                        :label="data.cancelLabel ?? 'Annuler'"
                        variant="outline"
                        color="neutral"
                        :disabled="loading"
                        @click="emit('close', false)"
                    />
                    <UButton
                        :label="data.confirmLabel ?? 'Confirmer'"
                        :color="isDanger ? 'error' : 'primary'"
                        :loading="loading"
                        :disabled="loading"
                        @click="emit('close', true)"
                    />
                </div>
            </div>
        </template>
    </UModal>
</template>