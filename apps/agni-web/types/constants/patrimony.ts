export type TypePatrimony = 'Asset' | 'Liability' 
export enum SourcePatrimonyType { 
    Fund = "Fund",
    Patrimony = "Patrimony",
    Provision = "Provision"
}

export function getLabelPatrimonyType(type: TypePatrimony) {
    if (type === 'Asset')
        return 'Actif'

    return 'Passif'
}