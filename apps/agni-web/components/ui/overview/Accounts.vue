<script setup lang="ts">
import { toAccountCard } from '~/mappers/account';
import type { AccountWithDetailType } from '~/types/ui/account';

const { accounts } = defineProps<{
    accounts: AccountWithDetailType[]
}>()

const emit = defineEmits<{
    (e: 'open', id: string): void    
    (e: 'update', id: string): void
    (e: 'delete', id: string): void
}>();

</script>

<template>
    <div >
        <h1 class="text-lg font-bold mb-5">Comptes</h1>
        <div class="bg-neutral-100 p-1 rounded-3xl">
            <UiOverviewCardAccount 
                v-for="(account, index) in accounts"
                :key="account.id"
                :account="toAccountCard(account)"
                @click="emit('open', account.id)"
                @update="emit('update', account.id)"
                @delete="emit('delete', account.id)"
                :displaydiveder="(accounts.length - 1) != index"
            />
        </div> 
    </div>
</template>