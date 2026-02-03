<script setup lang="ts">
const props = defineProps<{
  title: string
  titleResponsive: string
  isCollapsed?: boolean
}>()
</script>

<template>
  <div class="logo-container">
    <transition name="logo-fade" mode="out-in">
      <h1 v-if="!isCollapsed" key="full" class="logo-title">
        {{ title }}
      </h1>
      <h1 v-else key="short" class="logo-title-responsive">
        {{ titleResponsive }}
      </h1>
    </transition>
  </div>
</template>

<style scoped lang="scss">
.logo-container {
  display: flex;
  align-items: center;
  min-height: 40px;
}

.logo-title,
.logo-title-responsive {
  color: #1e3050;
  font-size: 2rem;
  font-weight: 800;
  margin: 0;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, #1e3050 0%, #6755d7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
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