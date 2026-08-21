export function generateUUID(): string {
  return crypto.randomUUID().toString()
}

export function generateShades(baseHue: number, count: number, saturation = 55): string[] {
  return Array.from({ length: count }, (_, i) => {
    // répartit la luminosité entre 25% (foncé) et 70% (clair)
    const lightness = 70 - (i * (45 / Math.max(count - 1, 1)))
    return `hsl(${baseHue}, ${saturation}%, ${lightness}%)`
  })
}