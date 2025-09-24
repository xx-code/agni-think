import { ValueError } from "../../../core/errors/valueError";
import { isStringDifferent } from "../helpers";
import { BudgetSaveGoal } from "../valueObjects/budgetSaveGoal";
import { ValueObjectCollection } from "../valueObjects/collection";
import { Scheduler } from "../valueObjects/scheduleInfo";
import Entity, { TrackableProperty } from "./entity";

export class Budget extends Entity { 
    private title: TrackableProperty<string> 
    private target: TrackableProperty<number>
    private scheduler: TrackableProperty<Scheduler>
    private isArchived: TrackableProperty<boolean>
    private targetSaveGoals: ValueObjectCollection<BudgetSaveGoal> 

    constructor(id: string, isArchive: boolean, target: number, 
        title: string, scheduler: Scheduler, targetSaveGoalIds: string[]) {
        super(id)
        this.target = new TrackableProperty<number>(target, this.markHasChange.bind(this)) 
        this.title = new TrackableProperty<string>(title, this.markHasChange.bind(this))
        this.scheduler = new TrackableProperty<Scheduler>(scheduler, this.markHasChange.bind(this))
        this.isArchived = new TrackableProperty<boolean>(isArchive, this.markHasChange.bind(this))
        this.targetSaveGoals = new ValueObjectCollection(targetSaveGoalIds.map(saveId => new BudgetSaveGoal(id, saveId)), this.markHasChange.bind(this))
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
    
    getSaveGoalIds(): string[] {
        return this.targetSaveGoals.get().map(i => i.saveGoalId)
    }

    setSaveGoalIds(saveGoals: string[]) {
        this.targetSaveGoals.set(saveGoals.map(saveId => new BudgetSaveGoal(this.getId(), saveId)))
    }

    addSaveGoal(saveId: string) {
        this.targetSaveGoals.add(new BudgetSaveGoal(this.getId(), saveId))
    }

    removeSaveGoal(saveId: string) {
        this.targetSaveGoals.delete(new BudgetSaveGoal(this.getId(), saveId))
    }
}
