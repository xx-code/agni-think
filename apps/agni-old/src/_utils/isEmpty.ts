export function isEmpty(value: any):boolean {
    if (value == undefined || value == null) {
        return true;
    }

    if (typeof value === 'string') {
        // Use trim() method to remove leading and trailing whitespaces
        const trimmedString = value.trim();

        // Check if the trimmed string is empty
        return !trimmedString.length;
    }
    
    return false
}