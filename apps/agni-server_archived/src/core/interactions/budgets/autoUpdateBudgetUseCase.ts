import { IUsecase } from "../interfaces";
import { MomentDateService } from "@core/domains/entities/libs";
import Repository, { BudgetFilter } from "@core/adapters/repository";
import { IEventRegister } from "@core/adapters/event";
import { Scheduler } from "@core/domains/valueObjects/scheduleInfo";
import { Budget } from "@core/domains/entities/budget";

export class AutoUpdateBudgetUseCase implements IUsecase<void, void> {
    private budgetRepo: Repository<Budget> 
    private eventManager: IEventRegister

    constructor(
        budgetRepo: Repository<Budget>,
        eventManager: IEventRegister
    ) {
        this.eventManager = eventManager
        this.budgetRepo = budgetRepo
    }

    async execute(): Promise<void> {
        try {

            const filterExtend = new BudgetFilter()
            filterExtend.schedulerDueDate = { date: new Date(Date.now()), comparator: "<="} 
            filterExtend.isArchive = false

            const budgets = await this.budgetRepo.getAll({
                limit: 0, offset: 0,
                queryAll: true
            }, filterExtend)

            for(let i = 0; i < budgets.items.length; i++) {
                const budget = budgets.items[i]

                console.log(budget)

                if (budget.getSchedule().repeater === undefined) {
                    budget.setIsArchive(true)
                } else {
                    const scheduler = budget.getSchedule()
                    const dueDate = MomentDateService.getUTCDateAddition(scheduler.dueDate, scheduler.repeater!.period, scheduler.repeater!.interval)
                    budget.reSchedule(new Scheduler(dueDate, { period: scheduler.repeater!.period, interval: scheduler.repeater!.interval}))
                }

                await this.budgetRepo.update(budget)

                this.eventManager.notify('notification', {
                    title: 'Budget',
                    content:`Le budget ${budget.getTitle()} a ete reinitializer`
                })
            }

        }
        catch (err: any){
            throw err
        }   
    }
}