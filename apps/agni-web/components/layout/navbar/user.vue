<script lang="ts" setup>
import type { DropdownMenuItem } from '@nuxt/ui'
const { user, logout } = useAuth()
const toast = useToast()

const items = ref<DropdownMenuItem[][]>([
    [
        {
            label: 'Logout',
            class: 'items-center',
            icon: 'i-lucide-log-out',
            color: 'error',
            kbds: ['shift', 'meta', 'q'],
            onSelect: async (e: Event) => {
                try {
                    e.preventDefault()
                    await logout()
                } catch(err: any) {
                    toast.add({
                        title: 'Connexion impossible',
                        description: err?.message ?? 'Vérifiez vos informations et réessayez.',
                        color: 'error',
                        icon: 'i-lucide-circle-alert'
                    })
                }
            }
        }
    ]
])

</script>

<template>
    <UDropdownMenu :items="items">
        <div class="flex items-center gap-2">
            <h1 class="cursor-pointer">{{ user?.userName }}</h1>
            <UAvatar :alt="user?.userName" size="md" />
        </div>
    </UDropdownMenu> 
</template>