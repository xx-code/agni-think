import { Money } from "./money"
import Entity, { TrackableProperty } from "./entity"
import { ValueObjectCollection } from "@core/domains/valueObjects/collection"
import SaveGoalItem from "../valueObjects/saveGoalItem"
import { isStringDifferent } from "../helpers"
import { ImportanceGoal, IntensityEmotionalDesir } from "../constants"

export class SaveGoal extends Entity {
    private title: TrackableProperty<string>
    private description: TrackableProperty<string>
    private target: TrackableProperty<Money>
    private balance: TrackableProperty<Money> 
    private desirValue: TrackableProperty<IntensityEmotionalDesir>
    private importance: TrackableProperty<ImportanceGoal>
    private wishDueDate: TrackableProperty<Date|undefined>
    private itemsToTracks: ValueObjectCollection<SaveGoalItem>
    private accountId: TrackableProperty<string|undefined>


    constructor(id: string, title: string, target: Money, balance: Money, 
        desirValue: IntensityEmotionalDesir, importance: ImportanceGoal, wishDueDate?:Date, items: SaveGoalItem[] = [], accountId?: string, description: string='') {
        super(id)
        this.title = new TrackableProperty<string>(title, this.markHasChange.bind(this))
        this.target = new TrackableProperty<Money>(target, this.markHasChange.bind(this))
        this.balance = new TrackableProperty<Money>(balance, this.markHasChange.bind(this))
        this.description = new TrackableProperty<string>(description, this.markHasChange.bind(this))
        this.itemsToTracks = new ValueObjectCollection<SaveGoalItem>(items, this.markHasChange.bind(this))
        this.desirValue = new TrackableProperty<IntensityEmotionalDesir>(desirValue, this.markHasChange.bind(this))
        this.importance = new TrackableProperty<ImportanceGoal>(importance, this.markHasChange.bind(this))
        this.wishDueDate = new TrackableProperty<Date|undefined>(wishDueDate, this.markHasChange.bind(this))
        this.accountId = new TrackableProperty<string|undefined>(accountId, this.markHasChange.bind(this)) 
    }
    
    setBalance(balance: Money) {
       this.balance.set(balance) 
    }

    getBalance(): Money {
        return this.balance.get()
    }

    increaseBalance(money: Money) {
        const newBalance = this.getBalance().add(money)
        this.balance.set(newBalance)
    }

    decreaseBalance(money: Money) {
        const newBalance = this.getBalance().subtract(money)
        this.balance.set(newBalance)
    }

    setTitle(title: string) {
        this.title.set(title, isStringDifferent)
    }

    getTitle(): string {
        return this.title.get()
    }

    setDescription(description: string) {
        this.description.set(description, isStringDifferent)
    }

    getDescription(): string {
        return this.description.get()
    }

    setTarget(target: Money) {
        this.target.set(target)
    }

    getTarget(): Money {
        return this.target.get()
    }

    getItems(): SaveGoalItem[] {
        return this.itemsToTracks.get()
    }

    setItems(items: SaveGoalItem[]) {
        return this.itemsToTracks.set(items)
    }

    addItem(item: SaveGoalItem) {
        this.itemsToTracks.add(item)
    }

    removeItem(item: SaveGoalItem) {
        this.itemsToTracks.delete(item)
    }

    setDesirValue(desirValue: IntensityEmotionalDesir) {
        this.desirValue.set(desirValue)
    }

    getDesirValue(): IntensityEmotionalDesir {
        return this.desirValue.get()
    }

    setImportance(importance: ImportanceGoal) {
        this.importance.set(importance)
    }
    
    getImportance(): ImportanceGoal {
        return this.importance.get()
    }

    setWishDueDate(wishDueDate?: Date) {
        this.wishDueDate.set(wishDueDate)
    }

    getWishDueDate(): Date|undefined {
        return this.wishDueDate.get()
    }

    setAccountId(accountid: string|undefined) {
        this.accountId.set(accountid)
    }

    getAccountId(): string|undefined {
        return this.accountId.get()
    }
}
