<script setup lang="ts">
const { title, titleResponsive } = defineProps<{
    title: string,
    titleResponsive: string
}>();
</script>

<template>
    <div>
        <h3 class="title">{{ title }}</h3>
        <h3 class="title-response">{{  titleResponsive }}</h3>
    </div>
</template>

<style scoped lang="scss">
.title {
    color: #1E3050;
    font-size: 2em;
    font-weight: bold; 
    @media (max-width: 975px) {
        display: none;
    }
}
.title-response {
    color: #1E3050;
    font-size: 2em;
    font-weight: bold;
    display: none;
    position: relative;
    left: -8px;
    text-align: cente;
    @media (max-width: 975px) {
        display: block;
    }
}
</style>