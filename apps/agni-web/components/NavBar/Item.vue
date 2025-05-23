<script setup lang="ts">
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { fas } from '@fortawesome/free-solid-svg-icons'
import { library } from '@fortawesome/fontawesome-svg-core'
import {  useRoute } from 'nuxt/app'
import { computed } from 'vue'


library.add(fas)

const props = defineProps({
    link: String,
    icon: String,
    title: String,
    isResponsive: Boolean
})

const route = useRoute()
const isActive = computed(() => route.path === props.link)



</script>

<template>
    <NuxtLink :href="link" style="text-decoration:none;" >
        <div class="link" :class="{'link-active': isActive}">
            <font-awesome-icon class="icon" style="" :icon="icon" />
            <p v-if="!isResponsive">{{ title }}</p>
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
            padding: 1rem;
        }

    }
    .icon {
        margin-right:5px; 
        font-size: 1rem;
        @media (max-width: 975px) {
            margin-right: 0; 
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