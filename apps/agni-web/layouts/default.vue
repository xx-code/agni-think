<template>
  <UApp>
    <UDashboardGroup class="h-screen w-full overflow-hidden">
      <LayoutSideBar />

      <div class="flex flex-col flex-1 w-full h-full min-w-0 min-h-0 relative">
        
        <div class="flex-none">
          <LayoutNavbar />
        </div>

        <main class="flex-1 overflow-y-auto min-w-0 bg-neutral-50 lg:rounded-tl-4xl lg:rounded-bl-4xl">
          <div class="p-4 lg:p-6 min-h-full max-w-7xl mx-auto">
            <slot />
          </div>
        </main>

        <div class="flex-none"> 
          <LayoutBottomBar />
        </div>

      </div>
    </UDashboardGroup>
  </UApp>
</template>
