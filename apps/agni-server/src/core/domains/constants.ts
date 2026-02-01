import { ValueError } from "@core/errors/valueError"

export enum RecordType {
    CREDIT = 'credit',
    DEBIT = 'Debit'
}

export enum Period {
    YEAR = 'Year',
    MONTH = 'Month',
    WEEK = 'Week',
    DAY = 'Day'
}

export enum AccountType {
    CHECKING = "Checking",
    CREDIT_CARD = "CreditCard",
    SAVING = "Saving",
    BUSINESS = "Business",
    BROKING = "Broking"
}

export enum HoldingType {
    ACTION = "Action", 
    FNB = "Fnb", 
    BOND = "Bond", 
    CRYPTO = "Crypto"
}

export enum ManagementAccountType {
    SELF_DIRECTED = "Self_directed", 
    MANAGED = "Managed", 
    ROBOT = "Robot"
}

export enum ContributionAccountType {
    REGISTERED = "Registered",    // REER, CELI, etc.
    UNREGISTERED  = "Unregistered"
}

export enum TransactionType {
    INCOME = "Income",
    FIXEDCOST = "FixedCost",
    VARIABLECOST = "VariableCost",
    OTHER = "Other"
}

export enum HoldingTransactionType {
    BUY = "Buy",
    SELL = "Sell", 
    DIVIDEND = "Dividend", 
    CONTRIBUTION = "Contribution", 
    FEE = "Fee", 
    TRANSFER = "Transfer"
}

export enum TransactionStatus {
    PENDING = "Pending",
    COMPLETE = "Complete"
}

export enum PatrimonyType {
    ASSET = "Asset",
    LIABILITY = "Liability"
}

export enum IntensityEmotionalDesir {
    INDIFFERENT = 0,
    PLEASURE = 1,
    DESIR = 2,
    OBSESSION = 3,
    FOMO = 4
} 

export enum ImportanceGoal {
    INSIGNIFIANT = 1,
    NORMAL = 2,
    IMPORTANT = 3,
    URGENT = 4
}

export enum EventType {
    NOTIFICATION
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
            throw new ValueError("PERIOD_NOT_VALID")
    }
}

export function mapperHoldingTransactionType(value: string): HoldingTransactionType {
    switch(value.toLowerCase()) {
        case HoldingTransactionType.BUY.toLowerCase():
            return HoldingTransactionType.BUY
        case HoldingTransactionType.SELL.toLowerCase():
            return HoldingTransactionType.SELL
        case HoldingTransactionType.CONTRIBUTION.toLowerCase():
            return HoldingTransactionType.CONTRIBUTION
        case HoldingTransactionType.DIVIDEND.toLowerCase():
            return HoldingTransactionType.DIVIDEND
        case HoldingTransactionType.FEE.toLowerCase():
            return HoldingTransactionType.FEE
        case HoldingTransactionType.TRANSFER.toLowerCase():
            return HoldingTransactionType.TRANSFER
        default:
            throw new ValueError("HOLDING_TRANSACTION_TYPE_NOT_VALID")
    }
}

export function mapperIntensityEmotionalDesir(value: number) {
    
}

export function mapperContributionAcccountType(value: string): ContributionAccountType {
    switch(value.toLowerCase()) {
        case ContributionAccountType.REGISTERED.toLowerCase():
            return ContributionAccountType.REGISTERED
        case ContributionAccountType.UNREGISTERED.toLowerCase():
            return ContributionAccountType.UNREGISTERED
        default:
            throw new ValueError("CONTRIBUTION_ACCOUNT_TYPE")
    }
}

export function mapperManagementAccountType(value: string): ManagementAccountType {
    switch(value.toLowerCase()) {
        case ManagementAccountType.SELF_DIRECTED.toLowerCase():
            return ManagementAccountType.SELF_DIRECTED
        case ManagementAccountType.MANAGED.toLowerCase():
            return ManagementAccountType.MANAGED
        case ManagementAccountType.ROBOT.toLowerCase():
            return ManagementAccountType.ROBOT
        default:
            throw new ValueError("NOT_CONTRIBUTION_ACCOUNT_TYPE")
    }
}

export function mapperHoldingType(value: string): HoldingType {
    switch(value.toLowerCase()) {
        case HoldingType.ACTION.toLowerCase():
            return HoldingType.ACTION 
        case HoldingType.BOND.toLowerCase():
            return HoldingType.BOND
        case HoldingType.CRYPTO.toLowerCase():
            return HoldingType.CRYPTO
        case HoldingType.FNB.toLowerCase():
            return HoldingType.FNB
        default:
            throw new ValueError("NOT_HOLDING_TYPE")
    }
}

export function mapperPatrimonyType(value: string): PatrimonyType {
    switch(value.toLowerCase()) {
        case PatrimonyType.ASSET.toLowerCase():
            return PatrimonyType.ASSET
        case PatrimonyType.LIABILITY.toLowerCase():
            return PatrimonyType.LIABILITY
        default:
            throw new ValueError("PATRIMONY_TYPE_NOT_VALID (" + value + ")")
    }
}

export function mapperTypeAccount(value: string): AccountType {
    switch(value.toLowerCase()) {
        case AccountType.CHECKING.toLowerCase():
            return AccountType.CHECKING
        case AccountType.BROKING.toLowerCase():
            return AccountType.BROKING
        case AccountType.SAVING.toLowerCase():
            return AccountType.SAVING
        case AccountType.BUSINESS.toLowerCase():
            return AccountType.BUSINESS
        case AccountType.CREDIT_CARD.toLowerCase():
            return AccountType.CREDIT_CARD
        default:
            throw new ValueError("ACCOUNT_TYPE_NOT_VALID")
    }
}

export function mapperRecordType(value: string): RecordType {
    switch (value.toLowerCase()) {
        case RecordType.CREDIT.toLowerCase():
            return RecordType.CREDIT
        case RecordType.DEBIT.toLowerCase():
            return RecordType.DEBIT
        default:
            throw new ValueError("RECORD_TYPE_NOT_VALID")
    }
}

export function mapperMainTransactionCategory(value: string) {
    switch (value.toLowerCase()) {
        case TransactionType.FIXEDCOST.toLowerCase():
            return TransactionType.FIXEDCOST
        case TransactionType.INCOME.toLowerCase():
            return TransactionType.INCOME
        case TransactionType.VARIABLECOST.toLowerCase():
            return TransactionType.VARIABLECOST
        case TransactionType.OTHER.toLowerCase():
            return TransactionType.OTHER
        default:
            throw new ValueError("TRANSACTION_TYPE_NOT_VALID")
    }
}

export function mapperTransactionStatus(value: string): TransactionStatus {
    switch(value.toLowerCase()) {
        case TransactionStatus.COMPLETE.toLowerCase():
            return TransactionStatus.COMPLETE
        case TransactionStatus.PENDING.toLowerCase():
            return TransactionStatus.PENDING
        default:
            throw new ValueError("TRANSACTION_STATUS_NOT_VALID")
    }
}

export const FREEZE_CATEGORY_ID = '1d462fd7-698d-4a08-a72a-1c96a5f82d50'

export const SAVING_CATEGORY_ID = '98a57fb1-c890-4059-b678-b8d0814fa7ec'

export const TRANSFERT_CATEGORY_ID = '6e57b8ed-2111-45d3-b55f-3994a40e7630'

export const TAG_SUBSCRIPTION_ID = 'ce64ec0d-a663-4f76-855f-ce67be9b6a5b'

export const DOLLAR_CURRENT_ID = '4be50a1e-71b9-4941-b00a-ca8409949736'

export type typePeriod = keyof typeof Period
