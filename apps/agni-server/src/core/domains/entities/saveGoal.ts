import { ValueError } from "@core/errors/valueError"
import { Money } from "./money"
import Entity, { TrackableProperty } from "./entity"
import { ValueObjectCollection } from "@core/domains/valueObjects/collection"
import SaveGoalItem from "../valueObjects/saveGoalItem"
import { isStringDifferent } from "../helpers"

export class SaveGoal extends Entity {
    private title: TrackableProperty<string>
    private description: TrackableProperty<string>
    private target: TrackableProperty<Money>
    private balance: TrackableProperty<Money> 
    private itemsToTracks: ValueObjectCollection<SaveGoalItem>


    constructor(id: string, title: string, target: Money, balance: Money, items: SaveGoalItem[] = [], description: string='') {
        super(id)
        this.title = new TrackableProperty<string>(title, this.markHasChange.bind(this))
        this.target = new TrackableProperty<Money>(target, this.markHasChange.bind(this))
        this.balance = new TrackableProperty<Money>(balance, this.markHasChange.bind(this))
        this.description = new TrackableProperty<string>(description, this.markHasChange.bind(this))
        this.itemsToTracks = new ValueObjectCollection<SaveGoalItem>(items, this.markHasChange.bind(this))
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

}
