<script setup lang="ts">
const isCollapsed = ref(false)
const isMobile = ref(false)

const navItems = [
  { title: 'Dashboard', link: '/', icon: 'i-lucide-circle-gauge' },
  { title: 'Transactions', link: '/invoices', icon: 'i-lucide-banknote' },
  { title: 'Wallets', link: '/wallets', icon: 'i-lucide-wallet-minimal' },
  { title: 'Schedule Transactions', link: '/schedule-invoices', icon: 'i-lucide-calendar-clock' },
  { title: 'Budgets', link: '/budgets', icon: 'i-lucide-wallet-cards' },
  { title: 'Goals', link: '/goals', icon: 'i-lucide-piggy-bank' },
  { title: 'Analytics', link: '/analytics', icon: 'i-lucide-chart-network' },
  { title: 'Patrimoine', link: '/patrimonies', icon: 'i-lucide-castle' },
  { title: 'Settings', link: '/settings', icon: 'i-lucide-settings' },
]

const checkScreenSize = () => {
  isMobile.value = window.innerWidth <= 975
  if (isMobile.value) {
    isCollapsed.value = true
  }
}

onMounted(() => {
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
})

const toggleNavbar = () => {
  if (!isMobile.value) {
    isCollapsed.value = !isCollapsed.value
  }
}
</script>

<template>
  <nav 
    class="navbar" 
    :class="{ 'navbar-collapsed': isCollapsed }"
    role="navigation"
    aria-label="Main navigation"
  >
    <div class="navbar-content">
      <!-- Header Section -->
      <div class="navbar-header">
        <NavBarLogoTitle 
          title="Agni." 
          title-responsive="A." 
          :is-collapsed="isCollapsed"
        />
        
        <button 
          v-if="!isMobile"
          class="collapse-toggle"
          @click="toggleNavbar"
          :aria-label="isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'"
        >
          <UIcon 
            :name="isCollapsed ? 'i-lucide-chevron-right' : 'i-lucide-chevron-left'" 
            size="lg"
          />
        </button>
      </div>

      <!-- Navigation Items -->
      <div class="navbar-items">
        <NavBarItem 
          v-for="item in navItems"
          :key="item.link"
          :title="item.title"
          :link="item.link"
          :icon="item.icon"
          :is-collapsed="isCollapsed"
        />
      </div>

      <!-- Footer Section (optional) -->
      <div class="navbar-footer">
        <!-- You can add user profile or logout here -->
      </div>
    </div>
  </nav>
</template>

<style scoped lang="scss">
.navbar {
  background-color: #ffffff;
  width: 260px;
  height: 100vh;
  position: sticky;
  top: 0;
  border-right: 1px solid #e5e7eb;
  transition: width 250ms cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.04);
  z-index: 100;

  &-collapsed {
    width: 80px;
  }

  @media (max-width: 975px) {
    width: 75px;
  }
}

.navbar-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 1.25rem 0.75rem;
  overflow-y: auto;
  overflow-x: hidden;

  /* Custom scrollbar */
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: #d1d5db;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: #9ca3af;
  }
}

.navbar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 0.75rem;
  margin-bottom: 2.5rem;
  position: relative;
}

.collapse-toggle {
  background: rgba(103, 85, 215, 0.1);
  border: none;
  border-radius: 8px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #6755d7;
  transition: all 200ms ease;
  position: absolute;
  right: -12px;

  &:hover {
    background: rgba(103, 85, 215, 0.2);
    transform: scale(1.05);
  }

  &:active {
    transform: scale(0.95);
  }

  @media (max-width: 975px) {
    display: none;
  }
}

.navbar-items {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.navbar-footer {
  margin-top: auto;
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
}
</style>