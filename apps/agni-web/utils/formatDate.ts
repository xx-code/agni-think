export function formatDate(date: Date): string {

    return date.toLocaleString('fr-Fr', {
        day: 'numeric',
        month: 'short',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
        timeZone: 'UTC'
    })
}