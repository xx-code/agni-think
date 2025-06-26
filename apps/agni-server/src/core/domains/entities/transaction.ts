import { TransactionMainCategory } from '@core/domains/constants';
import Entity, { TrackableProperty } from "./entity";
import { ValueObjectCollection } from "../valueObjects/collection";
import { TransactionBudget, TransactionTag } from "../valueObjects/transactions";

// Refactoring

export class Transaction extends Entity {
    private accountRef: TrackableProperty<string>
    private tagRefs: ValueObjectCollection<TransactionTag>
    private budgetRefs: ValueObjectCollection<TransactionBudget>
    private categoryRef: TrackableProperty<string>
    private recordRef: TrackableProperty<string>
    private date: TrackableProperty<string>
    private type: TrackableProperty<TransactionMainCategory>

    private isFreeze: boolean


    constructor(id: string, accountRef: string, recordRef: string, categoryRef: string,  date: string, 
        type: TransactionMainCategory, tagRefs: string[]=[], budgetRefs: string[]=[]) {
        super(id)
        this.accountRef = new TrackableProperty<string>(accountRef, this.markHasChange)
        this.recordRef = new TrackableProperty<string>(recordRef, this.markHasChange)
        this.tagRefs = new ValueObjectCollection(tagRefs.map(tag => new TransactionTag(tag)), this.markHasChange)
        this.budgetRefs = new ValueObjectCollection(budgetRefs.map(budget => new TransactionBudget(budget)), this.markHasChange)
        this.categoryRef = new TrackableProperty<string>(categoryRef, this.markHasChange)
        this.isFreeze = false;
        this.date = new TrackableProperty<string>(date, this.markHasChange)
        this.type = new TrackableProperty<TransactionMainCategory>(type, this.markHasChange)
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

    setAccountRef(accountRef: string) {
        this.accountRef.set(accountRef)
    }

    getAccountRef(): string {
        return this.accountRef.get()
    }

    setCategoryRef(categoryRef: string) {
        this.categoryRef.set(categoryRef)
    }

    setIsFreeze() {
        this.isFreeze = true
    }

    getIsFreeze(): boolean {
        return this.isFreeze
    }

    getCategoryRef(): string {
        return this.categoryRef.get()
    }

    setRecordRef(recordRef: string) {
        this.setRecordRef(recordRef)
    }

    getRecordRef(): string {
        return this.recordRef.get()
    }

    setDate(date: string) {
        this.date.set(date)
    }

    getDate(): string {
        return this.date.get()
    }

    setTransactionType(type: TransactionMainCategory) {
        this.type.set(type)
    }

    getTransactionType(): TransactionMainCategory {
        return this.type.get()
    }

}
