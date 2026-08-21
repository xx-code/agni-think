export type TypePatrimony = 'Asset' | 'Liability' 

export function getLabelPatrimonyType(type: TypePatrimony) {
    if (type === 'Asset')
        return 'Actif'

    return 'Passif'
}