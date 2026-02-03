import { RecordType } from "../constants"
import { isStringDifferent } from "../helpers"
import { ValueObjectCollection } from "../valueObjects/collection"
import { TransactionBudget, TransactionTag } from "../valueObjects/transactions"
import Entity, { TrackableProperty } from "./entity"
import { Money } from "./money"


export class Record extends Entity {
    private transactionId: string
    private tagRefs: ValueObjectCollection<TransactionTag>
    private budgetRefs: ValueObjectCollection<TransactionBudget>
    private categoryRef: TrackableProperty<string>
    private money: TrackableProperty<Money>   
    private description: TrackableProperty<string>

    constructor(id: string, transactionId: string, money: Money, categoryRef: string,  
        description: string = '', tagRefs: string[] = [], budgetRefs: string[] = []) {
        super(id)
        this.transactionId = transactionId
        this.money = new TrackableProperty(money, this.markHasChange.bind(this))
        this.description = new TrackableProperty(description, this.markHasChange.bind(this))

        this.tagRefs = new ValueObjectCollection(tagRefs.map(tag => new TransactionTag(tag)), this.markHasChange.bind(this))
        this.budgetRefs = new ValueObjectCollection(budgetRefs.map(budget => new TransactionBudget(budget)), this.markHasChange.bind(this))
        this.categoryRef = new TrackableProperty<string>(categoryRef, this.markHasChange.bind(this))
    }

    getTransactionId(): string {
        return this.transactionId
    }

    setMoney(money: Money) {
        this.money.set(money)
    }

    getMoney(): Money {
        return this.money.get()
    }  

    setDescription(description: string) {
        this.description.set(description, isStringDifferent)
    }

    getDescription(): string {
        return this.description.get()
    }

    setTags(tagRefs: string[]) {
        this.tagRefs.set(tagRefs.map(tag => new TransactionTag(tag)))  
    }
    
    setBudgets(budgetRefs: string[]) {
        this.budgetRefs.set(budgetRefs.map(budget => new TransactionBudget(budget))) 
    }
    
    addTag(tag: string) {
        this.tagRefs.add(new TransactionTag(tag)) 
    }

    addBudget(budget: string) {
        this.budgetRefs.add(new TransactionBudget(budget))
    }

    deleteTag(tag: string) {
        this.tagRefs.delete( new TransactionTag(tag))
    }

    deleteBudget(budget: string) {
        this.budgetRefs.delete(new TransactionBudget(budget))
    }

    getBudgetRefs(): string[] {
        return this.budgetRefs.get().map(budget => budget.budgetId)
    }

    getTags(): string[] {
        return this.tagRefs.get().map(tag => tag.tagId)
    }

    getCollectionTags(): ValueObjectCollection<TransactionTag> {
        return this.tagRefs
    }
    getCollectionBudgets(): ValueObjectCollection<TransactionBudget> {
        return this.budgetRefs
    }

    getCategoryRef(): string {
        return this.categoryRef.get()
    }

    setCategoryRef(categoryRef: string) {
        this.categoryRef.set(categoryRef)
    }
}