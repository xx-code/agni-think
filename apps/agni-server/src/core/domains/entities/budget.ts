import { ValueError } from "../../../core/errors/valueError";
import { isStringDifferent } from "../helpers";
import { Scheduler } from "../valueObjects/scheduleInfo";
import Entity, { TrackableProperty } from "./entity";

export class Budget extends Entity { 
    private title: TrackableProperty<string> 
    private target: TrackableProperty<number>
    private scheduler: TrackableProperty<Scheduler>
    private isArchived: TrackableProperty<boolean>

    constructor(id: string, isArchive: boolean, target: number, title: string, scheduler: Scheduler) {
        super(id)
        this.target = new TrackableProperty<number>(target, this.markHasChange.bind(this)) 
        this.title = new TrackableProperty<string>(title, this.markHasChange.bind(this))
        this.scheduler = new TrackableProperty<Scheduler>(scheduler, this.markHasChange.bind(this))
        this.isArchived = new TrackableProperty<boolean>(isArchive, this.markHasChange.bind(this))
    }

    setIsArchive(isArchive: boolean) {
        this.isArchived.set(isArchive)
    }

    getIsArchive(): boolean {
        return this.isArchived.get()
    }

    setTitle(title: string) {
        this.title.set(title, isStringDifferent)
    }

    getTitle(): string {
        return this.title.get()
    }

    setTarget(target: number) {
        if (target <= 0) 
            throw new ValueError("Target must be greater than 0")
        this.target.set(target)
    }

    getTarget(): number {
        return this.target.get()
    }

    reSchedule(newScheduler: Scheduler): void {
        this.scheduler.set(newScheduler, (a, b) => !a.isEqual(b))
    }

    getSchedule(): Scheduler {
        return this.scheduler.get()
    }

}
