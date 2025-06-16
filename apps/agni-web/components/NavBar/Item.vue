<script setup lang="ts">
import {  useRoute } from 'nuxt/app'


const props = defineProps({
    link: String,
    icon: String,
    title: String,
    isResponsive: Boolean
})

const route = useRoute()
const isActive = computed(() => route.path === props.link)

const title = computed(() => props.isResponsive ? '' : props.title)

</script>

<template>
    <NuxtLink :href="link" style="text-decoration:none;" >
        <div class="link" :class="{'link-active': isActive}">
            <UIcon class="icon" style="" :name="icon ? icon : ''" size="xl" />
            <p>{{ title }}</p>
        </div> 
    </NuxtLink>
</template>

<style scoped lang="scss">
    .link {
        color: #1E3050;
        font-size: 1rem;
        border-radius: 15px;
        display: flex;
        align-items: center;
        padding-left: 20px;
        padding-block: 0.9rem;
        &:hover {
            background: rgba(103, 85, 215, 0.1);
        }

        @media (max-width: 975px) {
            justify-content: center;
            padding-left: 0px;
        }

    }
    .icon {
        margin-right:5px; 
        @media (max-width: 975px) {
            
        }
    }
    .link-active {
        background-color: #6755D7;
        color: #fff;
        &:hover {
            background: #6755D7; 
        }
    }
</style>