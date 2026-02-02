<script setup lang="ts">
import { useCurrentRouteInfo } from '~/composables/routing'

const routeInfo = useCurrentRouteInfo()
const currentTime = ref(new Date())

// Update time every minute
onMounted(() => {
  const interval = setInterval(() => {
    currentTime.value = new Date()
  }, 60000)

  onUnmounted(() => clearInterval(interval))
})

const formattedDate = computed(() => {
  return currentTime.value.toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
})
</script>

<template>
  <div class="topbar">
    <div class="topbar-content">
      <!-- Page Info Section -->
      <div class="page-info">
        <div class="page-header">
          <h1 class="page-title">{{ routeInfo?.title }}</h1>
          <div class="breadcrumb">
            <span class="breadcrumb-text">{{ routeInfo?.subtitle }}</span>
          </div>
        </div>
        <p class="page-date">{{ formattedDate }}</p>
      </div>

      <!-- Actions Section -->
      <div class="topbar-actions">
        <!-- Search Bar (optional) -->
        <!-- <div class="search-container">
          <UInput 
            icon="i-lucide-search" 
            placeholder="Search..." 
            size="lg"
          />
        </div> -->

        <!-- Notification Icon -->
        <TopBarNotificationIcon />

        <!-- User Profile (optional) -->
        <!-- <div class="user-profile">
          <UAvatar 
            src="/api/placeholder/40/40" 
            alt="User" 
            size="md"
          />
        </div> -->
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.topbar {
  background-color: #ffffff;
  border-radius: 16px;
  padding: 1.5rem 1.75rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid #f1f3f5;
  margin-bottom: 1.5rem;

  @media (max-width: 768px) {
    padding: 1.25rem 1rem;
    border-radius: 12px;
  }
}

.topbar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1.5rem;

  @media (max-width: 768px) {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
}

.page-info {
  flex: 1;
  min-width: 0;
}

.page-header {
  margin-bottom: 0.5rem;
}

.page-title {
  color: #1e293b;
  font-size: 2rem;
  font-weight: 800;
  margin: 0 0 0.25rem 0;
  line-height: 1.2;
  letter-spacing: -0.5px;

  @media (max-width: 768px) {
    font-size: 1.75rem;
  }
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.breadcrumb-text {
  color: #64748b;
  font-size: 0.9rem;
  font-weight: 500;
}

.page-date {
  color: #94a3b8;
  font-size: 0.875rem;
  margin: 0;
  font-weight: 400;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 1rem;

  @media (max-width: 768px) {
    width: 100%;
    justify-content: flex-end;
  }
}

.search-container {
  width: 300px;

  @media (max-width: 1024px) {
    display: none;
  }
}

.user-profile {
  cursor: pointer;
  transition: transform 200ms ease;

  &:hover {
    transform: scale(1.05);
  }
}
</style>