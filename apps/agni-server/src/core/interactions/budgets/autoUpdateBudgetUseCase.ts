import { DateService } from "@core/adapters/libs";
import { Budget } from "@core/domains/entities/budget";
import { BudgetRepository } from "../../repositories/budgetRepository";

export interface IAutoUpdateBudgetUseCase {
    execute(): void
}

export interface IAutoUpdateBudgetPresenter {
    success(message: string): void;
    fail(err: Error): void;
}

export class AutoUpdateBudgetUseCase implements IAutoUpdateBudgetUseCase {
    private presenter: IAutoUpdateBudgetPresenter
    private budgetRepo: BudgetRepository
    private dateService: DateService

    constructor(presenter: IAutoUpdateBudgetPresenter, budgetRepo: BudgetRepository, dateService: DateService) {
        this.presenter = presenter
        this.budgetRepo = budgetRepo
        this.dateService = dateService
    }

    async execute(): Promise<void> {
        try {
            let budgets = await this.budgetRepo.getAll()

            budgets.forEach(async budget => {
                this.updateBudget(budget)
            })

            this.presenter.success('success')
        } catch(err: any) {
            this.presenter.fail(err)
        }
    }

    async updateBudget(budget: Budget): Promise<boolean> {
        return new Promise(async (resolve, reject) => {
            try {

                if (this.dateService.compareDate(this.dateService.getToday(), budget.getDateEnd() ?? '') > 0) {
                    await this.budgetRepo.toggleArchived(budget.getId(), true)
                    resolve(true)
                } else {
                    if (budget.getPeriod() !== null &&  budget.getPeriodTime() > 0)   
                        budget.setDateUpdate(this.dateService.getDateAddition(budget.getDateUpdate(), budget.getPeriod()!, budget.getPeriodTime()))

                    await this.budgetRepo.update(budget)
                }
                
                resolve(true)
            } catch(err: any) {
                console.log(err)
                resolve(false)
            }
        })
        
    }
}