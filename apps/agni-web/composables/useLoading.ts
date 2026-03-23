export const useLoading = () => {
  const isLoading = useState<boolean>('global-loading', () => false)

  const start = () => (isLoading.value = true)
  const stop = () => (isLoading.value = false)

  return { isLoading, start, stop }
}