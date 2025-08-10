export default function useApiLink() {
    const config = useRuntimeConfig();
    return config.public.apiBase;
}