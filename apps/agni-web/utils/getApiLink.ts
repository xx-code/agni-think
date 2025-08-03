export default function getApiLink() {
    const config = useRuntimeConfig()

    return config.public.api
} 