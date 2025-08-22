export default function computePercentage(total: number, remainder: number, limitTo100: boolean=true) {
    if (remainder === 0)
        return 0

    if (total === 0)
        return 0
    
    const result = (remainder/total) * 100
    if (!limitTo100 && result > 100)
        return 100

    return result
}