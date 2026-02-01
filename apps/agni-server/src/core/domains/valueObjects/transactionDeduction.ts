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
            const object: {
                transactionId: string, 
                deductionId: string
                amount: number
            } = value;

            const transDeduc = new TransactionDeduction(object.transactionId, object.deductionId, object.amount)

            return transDeduc
        } catch(err) {
            throw err
        }
    }
}