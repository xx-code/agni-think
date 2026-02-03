import { isStringDifferent } from "../helpers";
import Entity, { TrackableProperty } from "./entity";

export class Currency extends Entity {
    private name: TrackableProperty<string>
    private symbol: TrackableProperty<string>
    private locale: TrackableProperty<string | undefined>
    private rateToBase: TrackableProperty<number | undefined>
    private isBase: TrackableProperty<boolean | undefined>

    constructor(id: string, name: string, symbol: string, 
        locale?: string, rateToBase?: number, isBase?: boolean) {
        super(id)
        this.name = new TrackableProperty<string>(name, this.markHasChange.bind(this))
        this.symbol = new TrackableProperty<string>(symbol, this.markHasChange.bind(this))
        this.locale = new TrackableProperty<string|undefined>(locale, this.markHasChange.bind(this))
        this.rateToBase = new TrackableProperty<number|undefined>(rateToBase, this.markHasChange.bind(this))
        this.isBase = new TrackableProperty<boolean|undefined>(isBase, this.markHasChange.bind(this))
    }

    setName(name: string) {
        this.name.set(name, isStringDifferent)
    }

    getName(): string {
        return this.name.get()
    }

    setSymbol(symbol: string) {
        this.symbol.set(symbol)
    }

    getSymbol(): string {
        return this.symbol.get()
    }

    setLocale(locale: string | undefined) {
        this.locale.set(locale)
    }

    getLocale(): string | undefined {
        return this.locale.get()
    }

    setRateToBase(rateToBase: number|undefined) {
        this.rateToBase.set(rateToBase)
    }

    getRateToBase(): number|undefined{
        return this.rateToBase.get()
    }

    setIsBase(isBase: boolean|undefined) {
        this.isBase.set(isBase)
    }

    getIsBase(): boolean|undefined {
        return this.isBase.get()
    }
}