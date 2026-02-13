import ValueObject from "./valueObject";

export class TransactionDeduction extends ValueObject {
    public transactionId: string = '' 
    public deductionId: string = '' 
    public amount: number = 0 

    constructor(transactionId: string, deductionId: string, amount: number) {
        super()
        this.transactionId = transactionId
        this.deductionId = deductionId
        this.amount = amount
    }

    isEqual(object: TransactionDeduction): boolean {
        return this.deductionId === object.deductionId && this.transactionId === object.transactionId && this.amount === object.amount
    }

    toJson(): string {
        return JSON.stringify({
            transactionId: this.transactionId,
            deductionId: this.deductionId,
            amount: this.amount
        })
    }

    static fromJson(value: any): TransactionDeduction {
        try {
            if (!value) {
                throw new Error('Invalid deduction: empty value')
            }

            // JSONB arrivé en string
            if (typeof value === 'string') {
                try {
                value = JSON.parse(value)
                } catch {
                throw new Error(`Invalid deduction JSON string: ${value}`)
                }
            }

            if (typeof value !== 'object') {
                throw new Error('Invalid deduction: not an object')
            }

            const transactionId = value.transactionId ?? value.transaction_id
            const deductionId   = value.deductionId   ?? value.deduction_id
            const amount        = Number(value.amount)

            if (!deductionId || isNaN(amount)) {
                throw new Error(`Invalid deduction payload: ${JSON.stringify(value)}`)
            }

            return new TransactionDeduction(transactionId, deductionId, amount)
        } catch(err) {
            throw err
        }
    }
}

