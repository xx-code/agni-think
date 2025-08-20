export function parseAsLocalDate(dateStr: string): Date {
    const date = new Date(dateStr);
    return new Date(Date.UTC(
        date.getUTCFullYear(),
        date.getUTCMonth(),
        date.getUTCDate()
    ));
}