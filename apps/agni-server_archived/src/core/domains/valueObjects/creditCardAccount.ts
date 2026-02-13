import { AccountType } from "../constants";
import { IAccountDetail } from "../interface/accountDetail";
import ValueObject from "./valueObject";

export class CreditCardAccountDetail extends ValueObject implements IAccountDetail  {
    public creditLimite: number

    constructor(creditLimite: number) {
        super()
        this.creditLimite = creditLimite
    }

    isEqual(object: CreditCardAccountDetail): boolean {
        return this.creditLimite === object.creditLimite
    }

    toJson(): string {
        return JSON.stringify({
            credit_limit: this.creditLimite
        })
    }

    getJson() {
        return this.toJson()
    }

    static fromJson(value: any): CreditCardAccountDetail { 
        try {
            const object: {
                credit_limit: number, 
            } = value;

            return new CreditCardAccountDetail(object.credit_limit)
        } catch(err) {
            throw err
        }
   }

    getType(): AccountType {
        return AccountType.CREDIT_CARD
    }
}