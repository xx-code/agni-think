export class Money {
    private amount: number;
    private currency: string;


    constructor(amount: number = 0, currency: string = 'CAD') {
        if (amount < 0) {
            throw new Error("Resulting amount cannot be negative.");
        }

        this.amount = amount;
        this.currency = currency.toUpperCase();
    }

    getAmount(): number {
        return this.amount;
    }

    getCurrency(): string {
        return this.currency;
    }

    add(money: Money): Money {
        if (this.currency !== money.getCurrency()) {
            throw new Error("Currencies must match to add amounts.");
        }
        return new Money(this.amount + money.getAmount(), this.currency);
    }

    subtract(money: Money): Money {
        if (this.currency !== money.getCurrency()) {
            throw new Error("Currencies must match to subtract amounts.");
        }
        const result = this.amount - money.getAmount();
        if (result < 0) {
            throw new Error("Resulting amount cannot be negative.");
        }
        return new Money(result, this.currency);
    }

    convertTo(newCurrency: string, conversionRate: number): Money {
        if (conversionRate <= 0) {
            throw new Error("Conversion rate must be greater than 0.");
        }
        const convertedAmount = this.amount * conversionRate;
        return new Money(convertedAmount, newCurrency.toUpperCase());
    }

    toString(): string {
        return `${this.amount.toFixed(2)} ${this.currency}`;
    }

    equals(other: Money): boolean {
        return this.amount === other.amount && this.currency === other.currency;
    }
}
