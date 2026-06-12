<template>
    <UApp>
        <UDashboardGroup >
            <LayoutSideBar />
            <div class="flex flex-col w-full h-full relative">
                <div class="flex-none">
                    <LayoutNavbar class="" />
                </div>

                <div class="flex-1 overflow-auto">
                    <div class="lg:bg-neutral-50 lg:rounded-tl-4xl lg:rounded-bl-4xl h-full">
                        <slot />
                    </div>
                </div>

                <div class="flex-none"> 
                    <LayoutBottomBar class="" />
                </div>
            </div>
        </UDashboardGroup>
    </UApp>
    <!-- <ModalLoading /> -->
</template>
