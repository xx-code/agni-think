<script setup lang="ts">
definePageMeta({
    layout: false
})

const { handleCallback } = useAuth()
const router = useRouter()

onMounted(async () => {
    try {
        await handleCallback()
    } catch (err) {
        printLog('Callback error:', err)
        await router.replace('/login')
    }   
})
</script>

<template>
    <div class="w-screen h-screen">
        Callback...
    </div>
</template>