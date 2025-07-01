import { TransactionMainCategory } from "../constants";
import { ValueObjectCollection } from "../valueObjects/collection";
import { Scheduler } from "../valueObjects/scheduleInfo";
import { TransactionTag } from "../valueObjects/transactions";
import Entity, { TrackableProperty } from "./entity";

export class ScheduleTransaction extends Entity {
    private accountRef: TrackableProperty<string>
    private tagRefs: ValueObjectCollection<TransactionTag>
    private categoryRef: TrackableProperty<string>
    private recordRef: TrackableProperty<string>
    private type: TrackableProperty<TransactionMainCategory>
    private scheduler: TrackableProperty<Scheduler>

    constructor(id: string, accountRef: string, recordRef: string, categoryRef: string, 
        type: TransactionMainCategory, scheduler: Scheduler, tagRefs: string[]=[]) {
        super(id)
        this.accountRef = new TrackableProperty<string>(accountRef, this.markHasChange)
        this.recordRef = new TrackableProperty<string>(recordRef, this.markHasChange)
        this.tagRefs = new ValueObjectCollection(tagRefs.map(tag => new TransactionTag(tag)), this.markHasChange)
        this.categoryRef = new TrackableProperty<string>(categoryRef, this.markHasChange)
        this.type = new TrackableProperty<TransactionMainCategory>(type, this.markHasChange)
        this.scheduler = new TrackableProperty<Scheduler>(scheduler, this.markHasChange)
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

    setRecordRef(recordRef: string) {
        this.setRecordRef(recordRef)
    }

    getRecordRef(): string {
        return this.recordRef.get()
    }

    setTransactionType(type: TransactionMainCategory) {
        this.type.set(type)
    }

    getTransactionType(): TransactionMainCategory {
        return this.type.get()
    }

    reSchedule(newScheduler: Scheduler): void {
        this.scheduler.set(newScheduler, (a, b) => !a.isEqual(b))
    }

    getSchedule(): Scheduler {
        return this.scheduler.get()
    }
}