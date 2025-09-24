export type Space = {
    count: number
    spacing: number
}

export function getListTime(time: 'Year' |'Month' | 'Week', value: Space): string[] {
    if (time === 'Year')
        return getListYearTime(value)
    else if (time === 'Month')
        return getListMonthTime(value)
    else if (time === 'Week')
        return getListWeekTime(value)

    return []
}

function getListMonthTime(value: Space): string[] {
    const today = new Date();
    const months: string[] = [];
    const monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
    for(let i = 0; i < value.count; i++) {
        const d = new Date(today.getFullYear(), today.getMonth() - (i * value.spacing), 1);
        months.push(monthNames[d.getMonth()]);
    }
    return months.reverse();
}

function getListYearTime(value: Space): string[] {
    const today = new Date(Date.now())
    const years: string[] = []

    for(let i = 0; i < (value.count * value.spacing); i += value.spacing) {
        years.push(`An ${today.getFullYear() - i}`)
    }

    return years.reverse()
}

function getListWeekTime(value: Space): string[] {
    const today = new Date();
    const weeks: string[] = [];
    for(let i = 0; i < value.count; i++) {
        // Semaine ISO (lundi comme premier jour)
        const d = new Date(today);
        d.setDate(today.getDate() - (i * value.spacing * 7));
        // getWeek renvoie le numéro de la semaine
        const weekNumber = getWeekNumber(d);
        weeks.push(`Semaine ${weekNumber}`);
    }
    return weeks.reverse();
}

function getWeekNumber(date: Date): number {
    // Algorithme ISO 8601
    const d = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
    const dayNum = d.getUTCDay() || 7;
    d.setUTCDate(d.getUTCDate() + 4 - dayNum);
    const yearStart = new Date(Date.UTC(d.getUTCFullYear(),0,1));
    return Math.ceil((((d.getTime() - yearStart.getTime()) / 86400000) + 1)/7);
}