<script setup>
defineProps(['title'])
</script>

<template>
    <div>
        <h3 class="title">{{ title }}</h3>
    </div>
</template>

<style scoped>
.title {
    color: #1E3050;
    font-size: 2em;
    font-weight: bold;
}
</style>