export default function useApiAgentLink() {
    const config = useRuntimeConfig();
    return config.public.apiAgent;
}