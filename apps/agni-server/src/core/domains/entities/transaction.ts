import { ValueError } from "@core/errors/valueError";
import { TransactionMainCategory } from '@core/domains/constants';

// Refactoring

export class Transaction {
    private id: string;
    private accountRef: string
    private tagRefs: string[]
    private budgetRefs: string[]
    private categoryRef: string
    private recordRef: string
    private isFreeze: boolean
    private date: string
    private type: TransactionMainCategory

    private change: boolean = false

    __add_event_tag: string[] = []
    __delete_event_tag: string[] = []

    __add_event_budget: string[] = []
    __delete_event_budget: string[] = []

    constructor(id: string, accountRef: string, recordRef: string, categoryRef: string,  date: string, 
        type: TransactionMainCategory, tagRefs: string[]=[], budgetRefs: string[]=[]) {
        this.id = id 
        this.accountRef = accountRef
        this.recordRef = recordRef
        this.tagRefs = tagRefs
        this.budgetRefs = budgetRefs
        this.categoryRef = categoryRef
        this.isFreeze = false
        this.date = date
        this.type = type
    }

    setId(id: string) {
        this.id = id
    }

    getId(): string {
        return this.id
    }

    setTags(tagRefs: string[]) {
        // Check version
        let tag_to_add = tagRefs.filter(tag => this.tagRefs.findIndex(el => el === tag) === -1)
        let tag_to_delete = this.tagRefs.filter(tag => tagRefs.findIndex(compTag => compTag === tag) === -1)

        if (tag_to_add.length > 0 || tag_to_delete.length > 0) {
            this.__add_event_tag = tag_to_add
            this.__delete_event_tag = tag_to_delete

            this.change = true
            this.tagRefs = tagRefs
        }
    }
    
    setBudgets(budgetRefs: string[]) {
        // Check version
        let budget_to_add = budgetRefs.filter(budget => this.budgetRefs.findIndex(el => el === budget) === -1)
        let budget_to_delete = this.budgetRefs.filter(budget => budgetRefs.findIndex(compbudget => compbudget === budget) === -1)

        if (budget_to_add.length > 0 || budget_to_delete.length > 0) {
            this.__add_event_budget = budget_to_add
            this.__delete_event_budget = budget_to_delete

            this.change = true
            this.budgetRefs = budgetRefs
        }
    }
    
    addTag(tag: string) {
        if (this.tagRefs.includes(tag))
            throw new ValueError('Tag already exist, in transaction. Not duplicate allow.')
        this.__add_event_tag.push(tag)
        this.change = true
        this.tagRefs.push(tag)
    }

    addBudget(budget: string) {
        if (this.budgetRefs.includes(budget))
            throw new ValueError('Budget already exist, in transaction. Not duplicate allow.')
        this.__add_event_budget.push(budget)
        this.change = true
        this.budgetRefs.push(budget)
    }

    deleteTag(tag: string) {
        let index_tag = this.tagRefs.indexOf(tag)
        if (index_tag < 0)
            throw new ValueError('Tag do not exist, in Transaction.')
        this.__delete_event_tag.push(tag)
        this.change = true
        this.tagRefs.splice(index_tag, 1)
    }

    deleteBudget(budget: string) {
        let index_budget = this.budgetRefs.indexOf(budget)
        if (index_budget < 0)
            throw new ValueError('budget do not exist, in Transaction.')
        this.__delete_event_budget.push(budget)
        this.change = true
        this.budgetRefs.splice(index_budget, 1)
    }

    getBudgetRefs(): string[] {
        return this.budgetRefs
    }

    getTags(): string[] {
        return this.tagRefs
    }

    setAccountRef(accountRef: string) {
        if (this.accountRef !== accountRef)
            this.change = true
        this.accountRef = accountRef
    }

    getAccountRef(): string {
        return this.accountRef
    }

    setCategoryRef(categoryRef: string) {
        if (this.categoryRef !== categoryRef)
            this.change = true 
        this.categoryRef = categoryRef
    }

    setIsFreeze() {
        this.isFreeze = true
    }

    getIsFreeze(): boolean {
        return this.isFreeze
    }

    getCategoryRef(): string {
        return this.categoryRef
    }

    setRecordRef(recordRef: string) {
        if (this.recordRef !== recordRef)
            this.change = true 
        this.recordRef = recordRef
    }

    getRecordRef(): string {
        return this.recordRef
    }

    setDate(date: string) {
        if (this.date !== date) 
            this.change = true
        this.date = date
    }

    getDate(): string {
        return this.date
    }

    setTransactionType(type: TransactionMainCategory) {
        if (this.type !== type)
            this.change = true
        this.type = type;
    }

    getTransactionType(): TransactionMainCategory {
        return this.type
    }

    hasChange(): boolean {
        return this.change
    }
}
