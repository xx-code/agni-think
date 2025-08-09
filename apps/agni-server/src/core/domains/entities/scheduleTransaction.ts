import { TransactionType } from "../constants";
import { isStringDifferent } from "../helpers";
import { ValueObjectCollection } from "../valueObjects/collection";
import { Scheduler } from "../valueObjects/scheduleInfo";
import { TransactionTag } from "../valueObjects/transactions";
import Entity, { TrackableProperty } from "./entity";
import { Money } from "./money";

export class ScheduleTransaction extends Entity {
    private name: TrackableProperty<string>
    private accountRef: TrackableProperty<string>
    private tagRefs: ValueObjectCollection<TransactionTag>
    private categoryRef: TrackableProperty<string>
    private type: TrackableProperty<TransactionType>
    private scheduler: TrackableProperty<Scheduler>
    private amount: TrackableProperty<Money>
    private isPause: TrackableProperty<boolean>
    private isPay: TrackableProperty<boolean>
    
    constructor(id: string, name: string, accountRef: string, categoryRef: string, amount: Money, 
        type: TransactionType, scheduler: Scheduler, isPay:boolean=false, isPause: boolean=false, tagRefs: string[]=[]) {
        super(id)
        this.name = new TrackableProperty<string>(name, this.markHasChange.bind(this))
        this.amount = new TrackableProperty<Money>(amount, this.markHasChange.bind(this))
        this.accountRef = new TrackableProperty<string>(accountRef, this.markHasChange.bind(this))
        this.tagRefs = new ValueObjectCollection(tagRefs.map(tag => new TransactionTag(tag)), this.markHasChange.bind(this))
        this.categoryRef = new TrackableProperty<string>(categoryRef, this.markHasChange.bind(this))
        this.type = new TrackableProperty<TransactionType>(type, this.markHasChange.bind(this))
        this.scheduler = new TrackableProperty<Scheduler>(scheduler, this.markHasChange.bind(this))
        this.isPause = new TrackableProperty<boolean>(isPause, this.markHasChange.bind(this))
        this.isPay = new TrackableProperty<boolean>(isPay, this.markHasChange.bind(this))
    }

    setTags(tagRefs: string[]) {
        this.tagRefs.set(tagRefs.map(tag => new TransactionTag(tag)))  
    }
    
    
    addTag(tag: string) {
        this.tagRefs.add(new TransactionTag(tag)) 
    }

    deleteTag(tag: string) {
        this.tagRefs.delete( new TransactionTag(tag))
    }

    getTags(): string[] {
        return this.tagRefs.get().map(tag => tag.tagId)
    }

    getCollectionTags(): ValueObjectCollection<TransactionTag> {
        return this.tagRefs;
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

    getCategoryRef(): string {
        return this.categoryRef.get()
    }

    setTransactionType(type: TransactionType) {
        this.type.set(type)
    }

    getTransactionType(): TransactionType {
        return this.type.get()
    }

    reSchedule(newScheduler: Scheduler): void {
        this.scheduler.set(newScheduler, (a, b) => !a.isEqual(b))
    }

    getSchedule(): Scheduler {
        return this.scheduler.get()
    }

    setIsPause(isPause: boolean) {
        this.isPause.set(isPause)
    }

    getIsPause(): boolean {
        return this.isPause.get()
    }

    setIsPay(isPay: boolean) {
        this.isPay.set(isPay)
    }

    getIsPay(): boolean {
        return this.isPay.get()
    }

    setName(name: string) {
        this.name.set(name, isStringDifferent)
    }

    getName(): string {
        return this.name.get()
    }

    setAmount(amount: Money) {
        this.amount.set(amount)
    }

    getAmount(): Money {
        return this.amount.get()
    }
}