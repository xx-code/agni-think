export function formatDate(date: Date): string {

    return date.toLocaleString('fr-Fr', {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
        timeZone: 'UTC'
    })
}