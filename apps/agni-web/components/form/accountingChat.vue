<script lang="ts" setup>
    const model = defineModel<{message: String}>()
</script>

<template>
    <div class="flex justify-center items-center h-120 w-80">
        <UIcon name="i-lucide-bot" />
        {{ model?.message }}
    </div>
</template>