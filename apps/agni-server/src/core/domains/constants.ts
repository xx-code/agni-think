import { ValueError } from "@core/errors/valueError"
import { TransactionType } from "./entities/record"
import { isEmpty } from "./helpers"

export enum Period {
    YEAR = 'Year',
    MONTH = 'Month',
    WEEK = 'Week',
    DAY = 'Day'
}

export enum AccountType {
    CHECKING = "Checking",
    SAVING = "Saving",
    BUSINESS = "Business",
    BROKING = "Broking"
}

export const periodsSystem = [
    {
        name: 'Jour',
        value: Period.DAY
    },
    {
        name: 'Semaine',
        value: Period.WEEK
    },
    {
        name: 'Mois',
        value: Period.MONTH
    },
    {
        name: 'Année',
        value: Period.YEAR
    }
]

export const periodsBudget = [
    {
        name: 'Semaine',
        value: Period.WEEK
    },
    {
        name: 'Mois',
        value: Period.MONTH
    },
    {
        name: 'Année',
        value: Period.YEAR
    }
]

export function mapperPeriod(value: string): Period {
    switch(value.toLowerCase()) {
        case Period.YEAR.toLowerCase():
            return Period.YEAR
        case Period.MONTH.toLowerCase():
            return Period.MONTH
        case Period.WEEK.toLowerCase():
            return Period.WEEK
        case Period.DAY.toLowerCase():
            return Period.DAY
        default:
            throw new ValueError(`${value} is not a period`)
    }
}

export function mapperTypeAccount(value: string): AccountType {
    if (isEmpty(value)) {
        throw new ValueError('Type of account is empty')
    }

    switch(value.toLowerCase()) {
        case AccountType.CHECKING.toLowerCase():
            return AccountType.CHECKING
        case AccountType.BROKING.toLowerCase():
            return AccountType.BROKING
        case AccountType.SAVING.toLowerCase():
            return AccountType.SAVING
        case AccountType.BUSINESS.toLowerCase():
            return AccountType.BUSINESS
        default:
            throw new ValueError(`${value} is not a type of account`)
    }
}

export function mapperTransactionType(value: string): TransactionType {
    switch (value.toLowerCase()) {
        case TransactionType.CREDIT.toLowerCase():
            return TransactionType.CREDIT
        case TransactionType.DEBIT.toLowerCase():
            return TransactionType.DEBIT
        default:
            throw new ValueError(`${value} is not Credit or Debit`)
    }
}
 

export const FREEZE_CATEGORY_ID = '1d462fd7-698d-4a08-a72a-1c96a5f82d50'

export const SAVING_CATEGORY_ID = '98a57fb1-c890-4059-b678-b8d0814fa7ec'

export const TRANSFERT_CATEGORY_ID = '6e57b8ed-2111-45d3-b55f-3994a40e7630'

export type typePeriod = keyof typeof Period
