export function formatDate(date: Date): string {

    return date.toLocaleString('fr-Fr', {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
        timeZone: 'UTC'
    })
}

export function getDaysRemaining(dueDate: Date | string): number {
  const date = typeof dueDate === 'string' ? new Date(dueDate) : dueDate
  const today = new Date()
  const diffTime = date.getTime() - today.getTime()
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
}