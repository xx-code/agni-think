import ValueObject from "./valueObject";

export class PatrimonyAccount extends ValueObject {
    public accountId: string = '' 

    constructor(accountId: string) {
        super()
        this.accountId = accountId
    }

    isEqual(object: PatrimonyAccount): boolean {
        return this.accountId === object.accountId
    }

    toJson(): string {
        throw new Error("Method not implemented.");
    }

    static fromJson(string: string): PatrimonyAccount {
        throw new Error("Method not implemented.");
    }
}