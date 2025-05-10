export enum AccountType {
    CHECKING = "Checking",
    SAVING = "Saving",
    BUSINESS = "Business",
    BROKING = "Broking"
}

const enumLookup: { [key: string]: AccountType } = {
    "CHECKING": AccountType.CHECKING,
    "SAVING": AccountType.SAVING,
    "BUSINESS": AccountType.BUSINESS,
    "BROKING": AccountType.BROKING
};

export function convertStringToAccountType(value: string): AccountType | undefined {
    return enumLookup[value.toUpperCase()];
}

export type Account = {
    accountId: string
    name: string
    balance: number
    type: AccountType
} 