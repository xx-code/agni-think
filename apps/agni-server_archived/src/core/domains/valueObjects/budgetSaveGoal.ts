import ValueObject from "./valueObject";

export class BudgetSaveGoal implements ValueObject {
    public budgetId: string
    public saveGoalId: string  

    constructor(budgetId: string, saveGoalId: string) {
        this.budgetId = budgetId
        this.saveGoalId = saveGoalId
    }

    isEqual(object: BudgetSaveGoal): boolean {
        return this.budgetId === object.budgetId && this.saveGoalId === object.saveGoalId
    }
    toJson(): string {
        throw new Error("Method not implemented.");
    }

}