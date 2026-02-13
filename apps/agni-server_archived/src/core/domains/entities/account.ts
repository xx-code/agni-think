import UnExpectedError from "@core/errors/unExpectedError"
import { AccountType } from "../constants"
import { isStringDifferent } from "../helpers"
import Entity, { TrackableProperty } from "./entity"
import { Money } from "./money"
import { IAccountDetail } from "../interface/accountDetail"

export class Account extends Entity {
    private title: TrackableProperty<string>
    private balance: TrackableProperty<number>
    private type: TrackableProperty<AccountType>
    private detail: TrackableProperty<IAccountDetail|undefined> 
    private currencyId: TrackableProperty<string>

    constructor(id: string, title: string, type: AccountType, currencyId: string, detail?: IAccountDetail, balance: number = 0) {
        super(id)
        this.title = new TrackableProperty<string>(title, this.markHasChange.bind(this))
        this.type = new TrackableProperty<AccountType>(type, this.markHasChange.bind(this))
        this.balance = new TrackableProperty<number>(balance, this.markHasChange.bind(this))
        this.currencyId = new TrackableProperty<string>(currencyId, this.markHasChange.bind(this))

        if ((type == AccountType.BROKING || type == AccountType.CREDIT_CARD) && detail === undefined) 
            throw new UnExpectedError(`TYPE_ACCOUNT_MUST_HAVE_DETAIL_${type}`)

        this.detail = new TrackableProperty(detail, this.markHasChange.bind(this))
    }

    setTitle(title: string) {
        this.title.set(title, isStringDifferent)
    }

    getTitle(): string {
        return this.title.get()
    }

    setCurrencyId(currencyId: string) {
        this.currencyId.set(currencyId)
    }

    getCurrencyId(): string {
        return this.currencyId.get()
    }

    setType(type: AccountType) {
        this.type.set(type)
    }

    getType(): AccountType {
        return this.type.get()
    }

    setBalance(balance: number) {
        this.balance.set(balance)
    }

    getBalance(): number {
        return this.balance.get()
    }

    addOnBalance(money: Money) {
        const newBalance = this.getBalance() + money.getAmount()
        this.setBalance(newBalance)
    }

    substractBalance(money: Money) {
        const newBalance = this.getBalance() - money.getAmount()
        this.setBalance(newBalance)
    }

    getDetail(): IAccountDetail | undefined {
        return this.detail.get()
    }

    setDetail(detail: IAccountDetail) {
        this.detail.set(detail, (a, b) => !a?.isEqual(b!))
    }

    static m() {

    }
}
