<template>
    <UApp>
        <UDashboardGroup >
            <LayoutSideBar />
            <div class="w-full overflow-auto ">
                <LayoutNavbar />
                <div class="flex w-full pt-3">
                    <div class="lg:bg-neutral-50  lg:rounded-tl-4xl w-full">
                        <TopBar />
                        <slot />
                    </div>
                </div>
            </div>
        </UDashboardGroup>
    </UApp>
    <!-- <ModalLoading /> -->
</template>
