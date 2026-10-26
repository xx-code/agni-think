<script setup lang="ts">
import type { DropdownMenuItem } from '@nuxt/ui';
import { AccountType, getIconAccountType, getLabelAccountType } from '~/types/constants/account';
import type { AccountCard } from '~/types/ui/account';

const props  = withDefaults(
    defineProps<{
        account: AccountCard
        displaydiveder?: boolean
    }>(), {
        displaydiveder: true
    }
)

const emit = defineEmits<{
    update: [id: string]
    delete: [id: string]
}>()

 
const actionItems = ref<DropdownMenuItem[][]>([
    [
        {
            class: 'items-center',
            label: 'Modifier',
            icon: 'i-lucide-square-pen',
            onSelect: () => emit('update', props.account.id)
        },
    ],
    [
        {
            class: 'items-center',
            label: 'Supprimer',
            icon: 'i-lucide-trash',
            color: 'error',
            onSelect: () => emit('delete', props.account.id)
        }
    ]
])

const showNegativeIndicator = computed(() => {
    if (props.account.type != AccountType.CreditCard)
        return true 
    return false
})


</script>

<template>
    <div class="px-4">
        <div class="flex py-4 cursor-pointer items-start justify-between">
            <div class="flex items-center gap-2">
                <UAvatar :alt="account.title" size="lg" class="bg-white" />
                <div>
                    <h1 class="text-pretty text-primary">{{ account.title }}</h1>
                    <div class="flex items-center gap-1 text-xs">
                        <h5 class=" text-neutral-500">{{ getLabelAccountType(account.type)  }}</h5>
                        <UIcon :name="getIconAccountType(account.type)" />
                    </div>
                </div>
            </div> 

            <div class="flex items-center">
                <span :class="[
                    'text-primary',
                    showNegativeIndicator && account.balance < 0 ? 'text-danger' : ''
                ]">{{ formatCurrency(account.balance) }}</span>
                <UDropdownMenu :items="actionItems" >
                    <UButton 
                        @click.stop
                        icon="i-lucide-ellipsis-vertical"
                        variant="ghost"
                        color="neutral"
                    />
                </UDropdownMenu> 
            </div>
        </div>

        <div v-if="displaydiveder" class="bg-neutral-200 w-full h-px" />
    </div> 
</template>