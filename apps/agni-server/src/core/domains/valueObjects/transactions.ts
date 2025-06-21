import ValueObject from "./valueObject";

export class TransacationBudget extends ValueObject {
    public budgetId: string = ''

    isEqual(object: TransacationBudget): boolean {
        if (this.budgetId != object.budgetId)
            return false

        return true
    }
}

export class TransactionTag extends ValueObject {
    public tagId: string = ''

    isEqual(object: TransactionTag): boolean {
        if (this.tagId !== object.tagId)
            return false

        return true
    }
}