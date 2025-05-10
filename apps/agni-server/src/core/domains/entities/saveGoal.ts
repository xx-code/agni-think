import { ValueError } from "@core/errors/valueError"
import { Money } from "./money"

export class SaveGoal {
    private id: string
    private title: string
    private description: string
    private target: Money
    private balance: Money 
    private items: SaveGoalItem[]

    private change: boolean = false 

    __add_event_item: SaveGoalItem[] = []
    __del_event_item: string[] = []

    constructor(id: string, title: string, target: Money, balance: Money, items: SaveGoalItem[] = [], description: string='') {
        this.id = id
        this.title = title 
        this.target = target
        this.balance = balance
        this.description = description
        this.items = items
    }
    
    getId(): string {
        return this.id  
    }

    setBalance(balance: Money) {
        if (!balance.equals(balance))
            this.change = true
        this.balance = balance
    }

    getBalance(): Money {
        return this.balance
    }

    increaseBalance(money: Money) {
        this.balance = this.balance.add(money)
    }

    decreaseBalance(money: Money) {
        this.balance = this.balance.subtract(money)
    }

    setTitle(title: string) {
        if(this.title !== title)
            this.change = true

        this.title = title
    }

    getTitle(): string {
        return this.title
    }

    // setDepositAccount(depositeAccountId: string) {
    //     if (this.depositeAccountId !== depositeAccountId)
    //         this.change = true

    //     this.depositeAccountId = depositeAccountId
    // }

    // getDepositAccountId(): string {
    //     return this.depositeAccountId
    // }

    getDescription(): string {
        return this.description
    }

    setTarget(target: Money) {
        if (this.target.getAmount() !== target.getAmount())
            this.change = true

        this.target = target 
    }

    getTarget(): Money {
        return this.target
    }

    setDescription(description: string) {
        if (this.description !== description)
            this.change = true
        
        this.description = description
    }

    getItems(): SaveGoalItem[] {
        return this.items
    }

    setItems(items: SaveGoalItem[]) {
        // Check version
        let item_to_add = items.filter(item => this.items.findIndex(el => el.id === item.id) === -1)
        let item_to_delete = this.items.filter(el => items.findIndex(subEl => subEl.id === el.id) === -1)

        if (item_to_add.length > 0 || item_to_delete.length > 0) {
            this.__add_event_item = item_to_add
            this.__del_event_item = item_to_delete.map(el => el.id)

            this.change = true
            this.items = items
        }
    }

    addItem(item: SaveGoalItem) {
        this.change = true
        this.__add_event_item.push(item)
        this.items.push(item)
    }

    removeItem(itemId: string) {
        let index = this.items.findIndex(el => el.id === itemId)
        if (index === -1) 
            throw new ValueError("Can't remove item who not exist")
        this.change = true
        this.__del_event_item.push(itemId)
        this.items.splice(index, 1)
    }

    hasChange() {
        return this.change
    }
}
 
export type SaveGoalItem = {
    id: string
    title: string
    link: string
    price: Money
    htmlToTrack: string
}