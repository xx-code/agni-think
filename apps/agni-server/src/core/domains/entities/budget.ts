import { ValueError } from "../../../core/errors/valueError";
import { Period } from "../constants";
import { isEmpty } from "../helpers";

export class Budget {
    protected id: string
    protected isArchived: boolean
    protected title: string 
    protected target: number
    protected dateStart: string 
    protected categories: Array<string>
    protected tagRefs: Array<string> 
    protected dateUpdate: string 
    protected period: Period | null
    protected periodTime: number
    protected dateEnd: string

    private change: boolean = false

    __add_event_tag: string[] = []
    __delete_event_tag: string[] = []

    __add_event_category: string[] = []
    __delete_event_category: string[] = []

    // computeCurrentSpend(records: Record[]): Money {
    //     let price_records = records.map(record => record.amount);
    //     let current_spend = price_records.reduce((accumulator, current_value) => accumulator + current_value.getAmount(), 0);
        
    //     let to_money = new Money(current_spend)

    //     return to_money;
    // }

    constructor() {
        this.id = ""
        this.isArchived = false 
        this.target = 0
        this.title = ""
        this.dateStart = ""
        this.categories = []
        this.dateUpdate = ""
        this.tagRefs = []
        this.period = null
        this.periodTime = 0
        this.dateEnd = ""
    }

    initializer(id: string, isArchive: boolean, target: number, title: string, dateStart: string, period: Period | null, periodTime: number, dateEnd: string, dateUpdate: string, categories: string[] = [], tags: string[] = []) {
        this.id = id
        this.isArchived = isArchive
        this.target = target
        this.title = title
        this.dateStart = dateStart
        this.categories = categories
        this.tagRefs = tags
        this.period = period
        this.periodTime = periodTime
        this.dateUpdate = dateUpdate
        this.dateEnd = dateEnd
    }

    setId(id: string) {
        this.id = id
    }

    getId() {
        return this.id
    }

    setIsArchive(isArchive: boolean) {
        if (this.isArchived !== isArchive)
            this.change = true
        this.isArchived = isArchive
    }

    getIsArchive(): boolean {
        return this.isArchived
    }

    setTitle(title: string) {
        if (this.title !== title)
            this.change = true

        this.title = title
    }

    getTitle(): string {
        return this.title
    }

    setCategories(categories: string[]) {
        // Check version
        let category_to_add = categories.filter(cat => this.categories.findIndex(el => el === cat) === -1)
        let category_to_delete = this.categories.filter(el => categories.findIndex(subEl => subEl === el) === -1)

        if (category_to_add.length == 0 && category_to_delete.length  == 0)
            return 

        this.__add_event_category = category_to_add
        this.__delete_event_category = category_to_delete

        this.change = true
        this.categories = categories
    }

    getCategories(): string[] {
        return this.categories
    }

    setTags(tagRefs: string[]) {
        // Check version
        let tag_to_add = tagRefs.filter(tag => this.tagRefs.findIndex(el => el === tag) === -1)
        let tag_to_delete = this.tagRefs.filter(el => this.tagRefs.findIndex(subEl => subEl === el) === -1)

        if (tag_to_add.length > 0 || tag_to_delete.length > 0) {
            this.__add_event_tag = tag_to_add
            this.__delete_event_tag = tag_to_delete

            this.change = true
            this.tagRefs = tagRefs
        }
    }

    getTags(): string[] {
        return this.tagRefs
    }

    setTarget(target: number) {
        if (target <= 0) 
            throw new ValueError("Target must be greater than 0")
        
        if (this.target !== target)
            this.change = true 
        this.target = target
    }

    getTarget(): number {
        return this.target
    }


    setDateStart(dateStart: string) {
        if (this.dateStart !== dateStart)
            this.change = true 
        this.dateStart = dateStart
    }

    getDateStart(): string {
        return this.dateStart
    }

    setDateEnd(dateEnd: string) {
        if (this.dateEnd !== dateEnd)
            this.change = true 
        this.dateEnd = dateEnd
    }

    getDateEnd(): string {
        return this.dateEnd
    }


    setDateUpdate(dateUpdate: string) {
        if (this.dateUpdate !== dateUpdate)
            this.change = true 
        this.dateUpdate = dateUpdate
    }

    getDateUpdate(): string {
        return this.dateUpdate
    }
    
    addTag(tag: string) {
        if (this.tagRefs.includes(tag))
            throw new ValueError('Tag already exist, in Budget. Not duplicate allow.')
        this.__add_event_tag.push(tag)
        this.change = true
        this.tagRefs.push(tag)
    }

    deleteTag(tag: string) {
        let index_tag = this.tagRefs.indexOf(tag)
        if (index_tag < 0)
            throw new ValueError('Tag do not exist, in Budget.')
        this.__delete_event_tag.push(tag)
        this.change = true
        this.tagRefs.splice(index_tag, 1)
    }
    
    addCategory(category: string) {
        if (this.categories.includes(category))
            throw new ValueError('Category already exist, in Budget. Not duplicate allow.')

        this.__add_event_category.push(category)
        this.change = true 
        this.categories.push(category)
    }

    deleteCategory(category: string) {
        let index_category = this.categories.indexOf(category)

        if (index_category < 0)
            throw new ValueError('Tag do not exist, in Budget.')

        this.__delete_event_category.push(category)
        this.change = true
        this.categories.splice(index_category, 1)
    }

    setPeriod(period: Period|null) {
        if (this.period !== period)
            this.change = true 
        this.period = period
    }

    getPeriod(): Period | null {
        return this.period
    }

    setPeriodTime(periodTime: number) {
        if (periodTime <= 0)
            throw new ValueError("Period time must be greater than 0")

        if (this.periodTime !== periodTime)
            this.change = true

        this.periodTime = periodTime
    }

    getPeriodTime(): number {
        return this.periodTime
    }

    hasChange(): boolean {
        return this.change
    }
}

export interface IBudgetBuilder {
    reset(): void
    setId(id: string): void
    setTitle(title: string): void
    setTarget(target: number): void
    setIsArchived(isArchive: boolean): void
    setDateStart(dateStart: string): void
    setDateUpdate(dateUpdate: string): void
    setDateEnd(dateEnd: string): void
    setPeriod(period: Period): void
    setPeriodTime(period_time: number): void
    setCategories(category: Array<string>): void
    setTags(tags: Array<string>): void
}

export class BudgetBuilder implements IBudgetBuilder {
    budget: Budget | null = null

    constructor() {
        this.budget = new Budget()
    }

    setBudget(budget: Budget) {
        this.budget = budget
    }

    reset(): void {
        this.budget = null
    }
    setId(id: string): void {
        if (this.budget)
            this.budget.setId(id)
    }
    setIsArchived(isArchive: boolean): void {
        if (this.budget)
            this.budget.setIsArchive(isArchive)
    }
    setTitle(title: string): void {
        if (this.budget)
            this.budget.setTitle(title)
    }
    setTarget(target: number): void {
        if (this.budget)
            this.budget.setTarget(target)
    }
    setDateStart(dateStart: string): void {
        if (this.budget)
            this.budget.setDateStart(dateStart)
    }
    setDateUpdate(dateUpdate: string): void {
        if (this.budget)
            this.budget.setDateUpdate(dateUpdate)   
    }
    setDateEnd(dateEnd: string): void {
        if (this.budget)
            this.budget.setDateEnd(dateEnd)
    }
    setPeriod(period: Period): void {
        if (this.budget)
            this.budget.setPeriod(period)
    }
    setPeriodTime(periodTime: number): void {
        if (this.budget)
            this.budget.setPeriodTime(periodTime)
    }
    setCategories(categories: Array<string>): void {
        if (this.budget)
            this.budget.setCategories(categories)
    }
    setTags(tags: Array<string>): void {
        if (this.budget)
            this.budget.setTags(tags)
    }
    
    getBudget(additionDate: ((date: string, period: Period, periodTime: number) => string) | null = null): Budget | null {
        if (this.budget === null) 
            throw new ValueError("Builder can't build null budget")

        let budget = this.budget
        this.reset()
        

        if (isEmpty(budget.getDateEnd()) && budget.getPeriod() && budget.getPeriodTime() === 0) {
            throw new ValueError('this type of budget don\'t exit, an budget must have date of end or period count or all in same time')
        } 

        if (!isEmpty(budget.getDateEnd())) {
            budget.setDateUpdate(budget.getDateEnd())
        }
        
        if (budget.getPeriod()) {
            
            if (additionDate === null)
                throw new ValueError('Addition date function not define')

            if (budget.getPeriodTime() <= 0)
                throw new ValueError("Period not define")

            let dateUpdate = additionDate!(budget.getDateStart(), budget.getPeriod()!, budget.getPeriodTime())
            
            budget.setDateUpdate(dateUpdate)
        } 

        return budget
    }
}

