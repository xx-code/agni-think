<script setup lang="ts">
const props = defineProps({
    title: String
})
</script>

<template>
    <div class="custom-cart-title">
        <div>
            <h3>{{ title }}</h3>
        </div>
        <div>
            <slot />
        </div>
    </div>
</template>

<style scoped lang="scss">
.custom-cart-title {
    display: flex;
    align-items: center;
    justify-content: space-between;

    h3 {
        color: #1E3050;
        font-weight: bold;
        font-size: larger;
    }
}
</style>