<template>
  <UApp>
    <div class="app-layout">
      <NavBar />
      <main class="main-content">
        <UContainer class="content-wrapper">
          <TopBar />
          <div class="page-content">
            <slot />
          </div>
        </UContainer>
      </main>
    </div>
  </UApp>
</template>

<style scoped lang="scss">
.app-layout {
  min-height: 100vh;
  display: flex;
  background-color: #f8f9fa;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
}

.content-wrapper {
  flex: 1;
  padding: 1rem 1.5rem;
  max-width: 1400px;
  
  @media (max-width: 768px) {
    padding: 0.75rem 1rem;
  }
}

.page-content {
  margin-top: 1.5rem;
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>