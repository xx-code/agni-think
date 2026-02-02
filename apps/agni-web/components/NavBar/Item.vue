<script setup lang="ts">
import { useRoute } from 'nuxt/app'

const props = defineProps<{
  link: string
  icon: string
  title: string
  isCollapsed?: boolean
}>()

const route = useRoute()
const isActive = computed(() => route.path === props.link)
</script>

<template>
  <NuxtLink 
    :to="link" 
    class="nav-item-link"
    :aria-label="title"
    :title="isCollapsed ? title : ''"
  >
    <div class="nav-item" :class="{ 'nav-item-active': isActive, 'nav-item-collapsed': isCollapsed }">
      <UIcon class="nav-item-icon" :name="icon" size="xl" />
      <span v-if="!isCollapsed" class="nav-item-text">{{ title }}</span>
      
      <!-- Active indicator -->
      <div v-if="isActive" class="active-indicator" />
    </div>
  </NuxtLink>
</template>

<style scoped lang="scss">
.nav-item-link {
  text-decoration: none;
  display: block;
  position: relative;
}

.nav-item {
  color: #475569;
  font-size: 0.95rem;
  font-weight: 500;
  border-radius: 12px;
  display: flex;
  align-items: center;
  padding: 0.875rem 1rem;
  transition: all 200ms cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  cursor: pointer;
  gap: 0.75rem;

  &:hover {
    background: rgba(103, 85, 215, 0.08);
    color: #6755d7;
    transform: translateX(2px);
  }

  &-collapsed {
    justify-content: center;
    padding: 0.875rem 0.5rem;
  }

  &-active {
    background-color: #6755d7;
    color: #ffffff;
    box-shadow: 0 4px 12px rgba(103, 85, 215, 0.25);

    &:hover {
      background-color: #5a47c7;
      color: #ffffff;
      transform: translateX(2px);
    }

    .nav-item-icon {
      animation: iconPulse 0.6s ease;
    }
  }
}

.nav-item-icon {
  flex-shrink: 0;
  transition: transform 200ms ease;
  
  .nav-item:hover & {
    transform: scale(1.1);
  }
}

.nav-item-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.active-indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 60%;
  background: #ffffff;
  border-radius: 0 4px 4px 0;
  animation: slideIn 0.3s ease;
}

@keyframes iconPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.15);
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-50%) translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(-50%) translateX(0);
  }
}

@media (max-width: 975px) {
  .nav-item {
    justify-content: center;
    padding: 0.875rem 0.5rem;
  }

  .nav-item-text {
    display: none;
  }
}
</style>