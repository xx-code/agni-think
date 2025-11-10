export function creditUilisationToColor(creditUtilisation: number, idealPercent: number=15): string {
    if (creditUtilisation > idealPercent)
        return 'red' 
    
    return  'green'
}