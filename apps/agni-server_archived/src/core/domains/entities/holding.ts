import { HoldingType } from "../constants";
import Entity, { TrackableProperty } from "./entity";

export class Holding extends Entity {
    private accountId: string
    private title:TrackableProperty<string>
    private code:TrackableProperty<string>
    private type: TrackableProperty<HoldingType> 
    private currentPrice: TrackableProperty<number>
    private bookCost: TrackableProperty<number>
    private quantity: TrackableProperty<number>
    private lastUpdateDate: TrackableProperty<Date>

    constructor(id: string, accountId: string, title: string, 
        code: string, type: HoldingType, lastUpdateDate: Date,
        bookCost: number, quantity: number, currentPrice: number
    ) {
        super(id)
        this.accountId = accountId 
        this.title = new TrackableProperty<string>(title, this.markHasChange.bind(this))
        this.code = new TrackableProperty<string>(code, this.markHasChange.bind(this))
        this.type = new TrackableProperty<HoldingType>(type, this.markHasChange.bind(this))
        this.bookCost = new TrackableProperty<number>(bookCost, this.markHasChange.bind(this))
        this.quantity = new TrackableProperty<number>(quantity, this.markHasChange.bind(this))
        this.currentPrice = new TrackableProperty<number>(currentPrice, this.markHasChange.bind(this))
        this.lastUpdateDate = new TrackableProperty<Date>(lastUpdateDate, this.markHasChange.bind(this))
    }

    getAccountId(): string {
        return this.accountId
    }

    setTitle(title: string) {
        this.title.set(title)
    }

    getTitle(): string {
        return this.title.get()
    }

    setCode(code: string) {
        this.code.set(code)
    }

    getCode(): string {
        return this.code.get()
    }

    setType(type: HoldingType) {
        this.type.set(type)
    }

    getType(): HoldingType {
        return this.type.get()
    }

    setCurrentPrice(currentPrice: number) {
        this.currentPrice.set(currentPrice)
    }

    getCurrentPrice(): number {
        return this.currentPrice.get()
    }

    getMarketValue(): number {
        return this.quantity.get() * this.currentPrice.get()
    }

    setLastUpdateMarketValue(date: Date) {
        this.lastUpdateDate.set(date)
    }

    getLastUpdateMarketValue(): Date {
        return this.lastUpdateDate.get()
    }

    getBookCost(): number {
        return this.bookCost.get()
    }

    setBookCost(bookCost: number) {
        this.bookCost.set(bookCost)
    }

    getQuantity(): number {
        return this.quantity.get()
    }

    setQuantity(quantity: number) {
        this.quantity.set(quantity)
    }

    getTotalyield(): number {
        return this.getMarketValue() - this.getBookCost()
    }
}