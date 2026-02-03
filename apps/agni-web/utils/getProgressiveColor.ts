export function getProgressColor(percentage: number): 'success' | 'warning' | 'error' {
  if (percentage >= 90) return 'error'
  if (percentage >= 70) return 'warning'
  return 'success'
}
