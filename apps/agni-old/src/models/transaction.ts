import { Category } from "./category"
import { Tag } from "./tag"

export enum TransactionType {
    CREDIT = "Credit",
    DEBIT = "Debit"
}

const enumLookup: { [key: string]: TransactionType } = {
    "CREDIT": TransactionType.CREDIT,
    "DEBIT": TransactionType.DEBIT,
};

export function convertStringToTransactionType(value: string): TransactionType | undefined {
    return enumLookup[value.toUpperCase()];
}

export type Transaction = {
    accountId: string
    transactionId: string
    description: string
    category: Category
    tags: Tag[] 
    date: string
    amount: number
    type: TransactionType
}