import ValueObject from "./valueObject";

export class TransactionBudget extends ValueObject { 
    public budgetId: string = ''
    
    constructor(budgetId: string = '') {
        super();
        this.budgetId = budgetId;
    }

    isEqual(object: TransactionBudget): boolean {
        if (this.budgetId != object.budgetId)
            return false

        return true
    }

    toJson(): string {
        return JSON.stringify({budgetId: this.budgetId})
    }
    fromJson(string: string): TransactionBudget {
        throw new Error("Method not implemented.");
    }
}

export class TransactionTag extends ValueObject {
    public tagId: string = ''

    constructor(tagId: string = '') {
        super();
        this.tagId = tagId;
    }

    isEqual(object: TransactionTag): boolean {
        if (this.tagId !== object.tagId)
            return false

        return true
    }

    toJson(): string {
        return JSON.stringify({tagId: this.tagId})
    }
    fromJson(string: string): TransactionTag {
        throw new Error("Method not implemented.");
    }
}