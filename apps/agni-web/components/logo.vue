<script lang="ts" setup>
    const { title, titleResponsive, isCollapsed } = defineProps<{
        title: string
        titleResponsive: string
        isCollapsed?: boolean
    }>()
</script>

<template>
    <div>
        <transition name="logo-fade" mode="out-in">
            <h1 
                v-if="!isCollapsed" 
                key="full" 
                class="text-primary-700 font-bold text-2xl"> {{ title}} </h1>
        </transition>
    </div>  
</template>

<style lang="scss">
.logo-title, .logo-title-responsive {
    font-size: 2rem;
    font-weight: 800;
    margin: 0;
    letter-spacing: -0.5px;
}

.logo-title-responsive {
    font-size: 2rem;
}

.logo-fade-enter-active,
.logo-fade-leave-active {
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.logo-fade-enter-from {
    opacity: 0;
    transform: scale(0.8);
}

.logo-fade-leave-to {
    opacity: 0;
    transform: scale(0.8);
}

@media (max-width: 975px) {
    .logo-title {
        display: none;
    }
  
    .logo-title-responsive {
        display: block;
    }
}
</style>